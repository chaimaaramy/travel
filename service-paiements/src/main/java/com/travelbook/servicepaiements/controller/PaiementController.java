
package com.travelbook.servicepaiements.controller;

import com.travelbook.servicepaiements.dto.PaiementRequestDTO;
import com.travelbook.servicepaiements.dto.PaiementResponseDTO;
import com.travelbook.servicepaiements.service.PaiementService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @GetMapping
    public List<PaiementResponseDTO> getAllPaiements() {
        return paiementService.getAllPaiements();
    }

    @GetMapping("/{id}")
    public PaiementResponseDTO getPaiementById(
            @PathVariable Long id) {
        return paiementService.getPaiementById(id);
    }

    @GetMapping("/reservation/{reservationId}")
    public PaiementResponseDTO getPaiementByReservationId(
            @PathVariable Long reservationId) {
        return paiementService.getPaiementByReservationId(reservationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaiementResponseDTO createPaiement(
            @Valid @RequestBody PaiementRequestDTO dto) {
        return paiementService.createPaiement(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaiementResponseDTO> updatePaiement(
            @PathVariable Long id,
            @Valid @RequestBody PaiementRequestDTO dto) {

        return ResponseEntity.ok(
                paiementService.updatePaiement(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiement(
            @PathVariable Long id) {

        paiementService.deletePaiement(id);
        return ResponseEntity.noContent().build();
    }
}
