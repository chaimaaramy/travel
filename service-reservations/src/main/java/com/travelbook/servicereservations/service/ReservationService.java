
package com.travelbook.servicereservations.service;

import com.travelbook.servicereservations.dto.ReservationRequestDTO;
import com.travelbook.servicereservations.dto.ReservationResponseDTO;
import com.travelbook.servicereservations.entity.Reservation;
import com.travelbook.servicereservations.entity.StatutReservation;
import com.travelbook.servicereservations.repository.ReservationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(
            ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ReservationResponseDTO getReservationById(Long id) {
        return toDTO(findReservation(id));
    }

    public ReservationResponseDTO createReservation(
            ReservationRequestDTO dto) {

        if (dto.statut() != StatutReservation.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Une nouvelle réservation doit être EN_ATTENTE"
            );
        }

        Reservation reservation = new Reservation();

        reservation.setUserId(dto.userId());
        reservation.setVoyageId(dto.voyageId());
        reservation.setNombrePersonnes(dto.nombrePersonnes());
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        return toDTO(reservationRepository.save(reservation));
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
        Reservation reservation = findReservation(id);
        reservationRepository.delete(reservation);
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Réservation introuvable : " + id
                ));
    }

    private ReservationResponseDTO toDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getVoyageId(),
                reservation.getNombrePersonnes(),
                reservation.getDateReservation(),
                reservation.getStatut()
        );
    }
}
