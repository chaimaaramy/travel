package com.travelbook.servicereservations.service;

import com.travelbook.servicereservations.client.*;
import com.travelbook.servicereservations.dto.*;
import com.travelbook.servicereservations.entity.*;
import com.travelbook.servicereservations.repository.*;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final UtilisateurClient utilisateurClient;
    private final TrajetClient trajetClient;
    private final PaiementClient paiementClient;

    public ReservationService(ReservationRepository reservationRepository,
                              TicketRepository ticketRepository,
                              UtilisateurClient utilisateurClient,
                              TrajetClient trajetClient,
                              PaiementClient paiementClient) {
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.utilisateurClient = utilisateurClient;
        this.trajetClient = trajetClient;
        this.paiementClient = paiementClient;
    }

    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll().stream().map(this::toDTO).toList();
    }

    public ReservationResponseDTO getReservationById(Long id) {
        return toDTO(findReservation(id));
    }

    // ---------- Création avec vérifications, idempotence et compensation ----------
    public ReservationResponseDTO createReservation(
            ReservationRequestDTO dto, String idempotencyKey) {

        // 0. Idempotence : même clé = même réservation, pas de doublon
        Optional<Reservation> existante =
                reservationRepository.findByIdempotencyKey(idempotencyKey);
        if (existante.isPresent()) {
            return toDTO(existante.get());
        }

        if (dto.statut() != StatutReservation.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Une nouvelle réservation doit être EN_ATTENTE");
        }

        // 1. L'utilisateur existe-t-il ?
        verifierUtilisateur(dto.userId());

        // 2. Le trajet existe-t-il ?
        TrajetDTO trajet = recupererTrajet(dto.voyageId());

        // 3. Réserver les places (atomique côté service-trajets)
        reserverPlaces(dto.voyageId(), dto.nombrePersonnes());

        // 4. Enregistrer, et libérer les places si l'enregistrement échoue
        try {
            Reservation reservation = new Reservation();
            reservation.setUserId(dto.userId());
            reservation.setVoyageId(dto.voyageId());
            reservation.setNombrePersonnes(dto.nombrePersonnes());
            reservation.setDateReservation(LocalDateTime.now());
            reservation.setStatut(StatutReservation.EN_ATTENTE);
            reservation.setIdempotencyKey(idempotencyKey);
            reservation.setMontant(trajet.prix()
                    .multiply(BigDecimal.valueOf(dto.nombrePersonnes())));
            return toDTO(reservationRepository.save(reservation));
        } catch (RuntimeException e) {
            libererPlaces(dto.voyageId(), dto.nombrePersonnes());
            throw e;
        }
    }

    // ---------- Confirmation (appelée par service-paiements) : émet les billets ----------
    @Transactional
    public ReservationResponseDTO confirmerReservation(Long id) {
        Reservation reservation = findReservation(id);

        if (reservation.getStatut() == StatutReservation.CONFIRMEE) {
            return toDTO(reservation);
        }
        if (reservation.getStatut() != StatutReservation.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une réservation EN_ATTENTE peut être confirmée");
        }

        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepository.save(reservation);

        for (int place = 1; place <= reservation.getNombrePersonnes(); place++) {
            Ticket ticket = new Ticket();
            ticket.setReservationId(id);
            ticket.setNumeroPlace(place);
            ticket.setQrCode(UUID.randomUUID().toString());
            ticket.setEmisLe(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
        return toDTO(reservation);
    }

    // ---------- Annulation (compensation) : libère les places ----------
    public ReservationResponseDTO annulerReservation(Long id) {
        Reservation reservation = findReservation(id);

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            return toDTO(reservation);
        }
        libererPlaces(reservation.getVoyageId(), reservation.getNombrePersonnes());
        reservation.setStatut(StatutReservation.ANNULEE);
        return toDTO(reservationRepository.save(reservation));
    }

    // ---------- Consultation enrichie ----------
    public ReservationDetailsDTO getDetails(Long id) {
        Reservation reservation = findReservation(id);

        return new ReservationDetailsDTO(
                toDTO(reservation),
                optionnel(() -> utilisateurClient.getUser(reservation.getUserId())),
                optionnel(() -> trajetClient.getTrajet(reservation.getVoyageId())),
                ticketRepository.findByReservationId(id),
                optionnel(() -> paiementClient.getPaiementParReservation(id))
        );
    }

    public ReservationResponseDTO updateReservation(
            Long id, ReservationRequestDTO dto) {
        Reservation reservation = findReservation(id);
        reservation.setUserId(dto.userId());
        reservation.setVoyageId(dto.voyageId());
        reservation.setNombrePersonnes(dto.nombrePersonnes());
        reservation.setStatut(dto.statut());
        return toDTO(reservationRepository.save(reservation));
    }

    public void deleteReservation(Long id) {
        reservationRepository.delete(findReservation(id));
    }

    // ---------- Appels distants et gestion des erreurs ----------
    private void verifierUtilisateur(Long userId) {
        try {
            utilisateurClient.getUser(userId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Utilisateur introuvable : " + userId);
        } catch (FeignException e) {
            throw indisponible("service-utilisateurs");
        }
    }

    private TrajetDTO recupererTrajet(Long trajetId) {
        try {
            return trajetClient.getTrajet(trajetId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Trajet introuvable : " + trajetId);
        } catch (FeignException e) {
            throw indisponible("service-trajets");
        }
    }

    private void reserverPlaces(Long trajetId, int nombre) {
        try {
            trajetClient.reserverPlaces(trajetId, nombre);
        } catch (FeignException.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pas assez de places disponibles");
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Trajet introuvable : " + trajetId);
        } catch (FeignException e) {
            throw indisponible("service-trajets");
        }
    }

    private void libererPlaces(Long trajetId, int nombre) {
        try {
            trajetClient.libererPlaces(trajetId, nombre);
        } catch (FeignException e) {
            throw indisponible("service-trajets");
        }
    }

    private ResponseStatusException indisponible(String service) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                service + " est indisponible");
    }

    private <T> T optionnel(Supplier<T> appel) {
        try {
            return appel.get();
        } catch (FeignException e) {
            return null;
        }
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Réservation introuvable : " + id));
    }

    private ReservationResponseDTO toDTO(Reservation r) {
        return new ReservationResponseDTO(
                r.getId(), r.getUserId(), r.getVoyageId(),
                r.getNombrePersonnes(), r.getDateReservation(),
                r.getStatut(), r.getMontant());
    }
}