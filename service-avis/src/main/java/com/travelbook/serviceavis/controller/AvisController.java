package com.travelbook.serviceavis.controller;


import com.travelbook.serviceavis.dto.AvisRequestDTO;
import com.travelbook.serviceavis.dto.AvisResponseDTO;
import com.travelbook.serviceavis.service.AvisService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    @GetMapping
    public List<AvisResponseDTO> getAllAvis() {
        return avisService.getAllAvis();
    }

    @GetMapping("/{id}")
    public AvisResponseDTO getAvisById(@PathVariable Long id) {
        return avisService.getAvisById(id);
    }

    @GetMapping("/voyage/{voyageId}")
    public List<AvisResponseDTO> getAvisByVoyageId(
            @PathVariable Long voyageId) {
        return avisService.getAvisByVoyageId(voyageId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvisResponseDTO createAvis(
            @Valid @RequestBody AvisRequestDTO dto) {
        return avisService.createAvis(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvisResponseDTO> updateAvis(
            @PathVariable Long id,
            @Valid @RequestBody AvisRequestDTO dto) {

        return ResponseEntity.ok(
                avisService.updateAvis(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvis(@PathVariable Long id) {
        avisService.deleteAvis(id);
        return ResponseEntity.noContent().build();
    }
}
