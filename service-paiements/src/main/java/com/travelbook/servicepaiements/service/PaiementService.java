
package com.travelbook.servicepaiements.service;

import com.travelbook.servicepaiements.dto.NotificationEmailDto;
import com.travelbook.servicepaiements.dto.PaiementRequestDTO;
import com.travelbook.servicepaiements.dto.PaiementResponseDTO;
import com.travelbook.servicepaiements.client.ReservationClient;
import com.travelbook.servicepaiements.client.ReservationDTO;
import com.travelbook.servicepaiements.client.UtilisateurClient;
import com.travelbook.servicepaiements.client.UtilisateurDTO;
import com.travelbook.servicepaiements.entity.Paiement;
import com.travelbook.servicepaiements.entity.StatutPaiement;
import com.travelbook.servicepaiements.messaging.NotificationPublisher;
import com.travelbook.servicepaiements.repository.PaiementRepository;
import com.travelbook.servicepaiements.resilience.ResilientRequests;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Service
public class PaiementService {

    private static final Logger log = LoggerFactory.getLogger(PaiementService.class);

    private final PaiementRepository paiementRepository;
    private final ReservationClient reservationClient;
    private final UtilisateurClient utilisateurClient;
    private final NotificationPublisher notificationPublisher;
    private final ResilientRequests resilientRequests;

    public PaiementService(PaiementRepository paiementRepository,
                           ReservationClient reservationClient,
                           UtilisateurClient utilisateurClient,
                           NotificationPublisher notificationPublisher,
                           ResilientRequests resilientRequests) {
        this.paiementRepository = paiementRepository;
        this.reservationClient = reservationClient;
        this.utilisateurClient = utilisateurClient;
        this.notificationPublisher = notificationPublisher;
        this.resilientRequests = resilientRequests;
    }

    public List<PaiementResponseDTO> getAllPaiements() {
        return paiementRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public PaiementResponseDTO getPaiementById(Long id) {
        return toDTO(findPaiement(id));
    }

    public PaiementResponseDTO getPaiementByReservationId(Long reservationId) {
        return paiementRepository.findByReservationId(reservationId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paiement introuvable pour la réservation : " + reservationId
                ));
    }

    public PaiementResponseDTO createPaiement(
            PaiementRequestDTO dto) {

        if (dto.statut() != StatutPaiement.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le paiement doit commencer EN_ATTENTE"
            );
        }

        if (paiementRepository.findByReservationId(dto.reservationId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un paiement existe déjà pour cette réservation"
            );
        }
        ReservationDTO reservation = recupererReservation(dto.reservationId());
        if (!"EN_ATTENTE".equals(reservation.statut())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La réservation doit être EN_ATTENTE pour être payée"
            );
        }
        if (reservation.montant() == null
                || reservation.montant().compareTo(dto.montant()) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le montant ne correspond pas à celui de la réservation"
            );
        }

        Paiement paiement = new Paiement();

        paiement.setReservationId(dto.reservationId());
        paiement.setMontant(dto.montant());
        paiement.setModePaiement(dto.modePaiement());
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        paiement.setDatePaiement(LocalDateTime.now());

        return toDTO(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementResponseDTO updatePaiement(
            Long id, PaiementRequestDTO dto) {

        Paiement paiement = findPaiement(id);

        if (!paiement.getReservationId().equals(dto.reservationId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La réservation d'un paiement ne peut pas être modifiée"
            );
        }
        if (paiement.getStatut() == dto.statut()) {
            return toDTO(paiement);
        }
        if (paiement.getStatut() != StatutPaiement.EN_ATTENTE
                || (dto.statut() != StatutPaiement.PAYE
                && dto.statut() != StatutPaiement.REFUSE)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement EN_ATTENTE peut devenir PAYE ou REFUSE"
            );
        }

        ReservationDTO reservation = recupererReservation(paiement.getReservationId());
        if (dto.statut() == StatutPaiement.PAYE) {
            appelerReservation(() -> reservationClient.confirmerReservation(
                    paiement.getReservationId()));
        } else {
            appelerReservation(() -> reservationClient.annulerReservation(
                    paiement.getReservationId()));
        }

        paiement.setStatut(dto.statut());
        Paiement paiementEnregistre = paiementRepository.save(paiement);
        preparerNotificationApresCommit(paiementEnregistre, reservation);

        return toDTO(paiementEnregistre);
    }

    public void deletePaiement(Long id) {
        Paiement paiement = findPaiement(id);
        paiementRepository.delete(paiement);
    }

    private Paiement findPaiement(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paiement introuvable : " + id
                ));
    }

    private ReservationDTO recupererReservation(Long reservationId) {
        try {
            return resilientRequests.lectureObligatoire("service-reservations",
                    () -> reservationClient.getReservation(reservationId));
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Réservation introuvable : " + reservationId
            );
        } catch (FeignException e) {
            throw indisponible();
        }
    }

    private void appelerReservation(Supplier<ReservationDTO> appel) {
        try {
            resilientRequests.ecritureObligatoire("service-reservations", appel);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Réservation introuvable"
            );
        } catch (FeignException.Conflict e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La réservation ne peut pas changer d'état"
            );
        } catch (FeignException e) {
            throw indisponible();
        }
    }

    private ResponseStatusException indisponible() {
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "service-reservations est indisponible"
        );
    }

    private void preparerNotificationApresCommit(
            Paiement paiement, ReservationDTO reservation) {
        UtilisateurDTO utilisateur = resilientRequests.lectureOptionnelle(
                "service-utilisateurs",
                () -> utilisateurClient.getUtilisateur(reservation.userId()));
        if (utilisateur == null) {
            log.warn("Paiement {} enregistré, notification ignorée : "
                    + "service-utilisateurs indisponible ou utilisateur introuvable",
                    paiement.getId());
            return;
        }

        if (utilisateur.email() == null || utilisateur.email().isBlank()) {
            log.warn("Paiement {} enregistré, notification ignorée : "
                    + "l'utilisateur {} n'a pas d'adresse email",
                    paiement.getId(), reservation.userId());
            return;
        }

        boolean paye = paiement.getStatut() == StatutPaiement.PAYE;
        String objet = paye ? "Paiement confirmé" : "Paiement refusé";
        String contenu = paye
                ? "Bonjour " + utilisateur.nom() + ", votre réservation "
                + paiement.getReservationId() + " est confirmée. Montant : "
                + paiement.getMontant() + "."
                : "Bonjour " + utilisateur.nom() + ", votre paiement pour la réservation "
                + paiement.getReservationId() + " a été refusé.";
        NotificationEmailDto notification = new NotificationEmailDto(
                utilisateur.email(), objet, contenu);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            notificationPublisher.envoyer(notification);
                        } catch (org.springframework.amqp.AmqpException e) {
                            log.error("Paiement {} enregistré, mais publication "
                                    + "de la notification impossible",
                                    paiement.getId(), e);
                        }
                    }
                });
    }

    private PaiementResponseDTO toDTO(Paiement paiement) {
        return new PaiementResponseDTO(
                paiement.getId(),
                paiement.getReservationId(),
                paiement.getMontant(),
                paiement.getModePaiement(),
                paiement.getStatut(),
                paiement.getDatePaiement()
        );
    }
}
