package com.travelbook.servicetrajets.controller;

import com.travelbook.servicetrajets.dto.TrajetRequestDTO;
import com.travelbook.servicetrajets.dto.TrajetResponseDTO;
import com.travelbook.servicetrajets.service.TrajetService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetService trajetService;

    public TrajetController(TrajetService trajetService) {
        this.trajetService = trajetService;
    }

        @PostMapping("/{id}/reserver-places")
    public ResponseEntity<Void> reserverPlaces(
            @PathVariable Long id, @RequestParam int nombre) {
        trajetService.reserverPlaces(id, nombre);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/liberer-places")
    public ResponseEntity<Void> libererPlaces(
            @PathVariable Long id, @RequestParam int nombre) {
        trajetService.libererPlaces(id, nombre);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    public List<TrajetResponseDTO> getAllTrajets() {
        return trajetService.getAllTrajets();
    }

    @GetMapping("/{id}")
    public TrajetResponseDTO getTrajetById(@PathVariable Long id) {
        return trajetService.getTrajetById(id);
    }

    @GetMapping("/voyage/{voyageId}")
    public List<TrajetResponseDTO> getTrajetsByVoyageId(
            @PathVariable Long voyageId) {

        return trajetService.getTrajetsByVoyageId(voyageId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrajetResponseDTO createTrajet(
            @Valid @RequestBody TrajetRequestDTO dto) {

        return trajetService.createTrajet(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrajetResponseDTO> updateTrajet(
            @PathVariable Long id,
            @Valid @RequestBody TrajetRequestDTO dto) {

        return ResponseEntity.ok(
                trajetService.updateTrajet(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrajet(@PathVariable Long id) {

        trajetService.deleteTrajet(id);
        return ResponseEntity.noContent().build();
    }
}
