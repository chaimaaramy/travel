
package com.travelbook.servicereservations.controller;

import com.travelbook.servicereservations.dto.ReservationRequestDTO;
import com.travelbook.servicereservations.dto.ReservationResponseDTO;
import com.travelbook.servicereservations.service.ReservationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ReservationResponseDTO getReservationById(
            @PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDTO createReservation(
            @Valid @RequestBody ReservationRequestDTO dto) {
        return reservationService.createReservation(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequestDTO dto) {

        return ResponseEntity.ok(
                reservationService.updateReservation(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
