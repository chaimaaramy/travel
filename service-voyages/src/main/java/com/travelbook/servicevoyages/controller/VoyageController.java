
package com.travelbook.servicevoyages.controller;

import com.travelbook.servicevoyages.dto.VoyageRequestDTO;
import com.travelbook.servicevoyages.dto.VoyageResponseDTO;
import com.travelbook.servicevoyages.service.VoyageService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voyages")
public class VoyageController {

    private final VoyageService voyageService;

    public VoyageController(VoyageService voyageService) {
        this.voyageService = voyageService;
    }

    @GetMapping
    public List<VoyageResponseDTO> getAllVoyages() {
        return voyageService.getAllVoyages();
    }

    @GetMapping("/{id}")
    public VoyageResponseDTO getVoyageById(@PathVariable Long id) {
        return voyageService.getVoyageById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoyageResponseDTO createVoyage(
            @Valid @RequestBody VoyageRequestDTO dto) {
        return voyageService.createVoyage(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoyageResponseDTO> updateVoyage(
            @PathVariable Long id,
            @Valid @RequestBody VoyageRequestDTO dto) {
        return ResponseEntity.ok(
                voyageService.updateVoyage(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoyage(@PathVariable Long id) {
        voyageService.deleteVoyage(id);
        return ResponseEntity.noContent().build();
    }
}
