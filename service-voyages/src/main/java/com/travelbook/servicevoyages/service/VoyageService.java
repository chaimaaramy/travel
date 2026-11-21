
package com.travelbook.servicevoyages.service;

import com.travelbook.servicevoyages.dto.VoyageRequestDTO;
import com.travelbook.servicevoyages.dto.VoyageResponseDTO;
import com.travelbook.servicevoyages.entity.Voyage;
import com.travelbook.servicevoyages.repository.VoyageRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VoyageService {

    private final VoyageRepository voyageRepository;

    public VoyageService(VoyageRepository voyageRepository) {
        this.voyageRepository = voyageRepository;
    }

    public List<VoyageResponseDTO> getAllVoyages() {
        return voyageRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public VoyageResponseDTO getVoyageById(Long id) {
        return toDTO(findVoyage(id));
    }

    public VoyageResponseDTO createVoyage(VoyageRequestDTO dto) {
        Voyage voyage = new Voyage();
        updateEntity(voyage, dto);
        return toDTO(voyageRepository.save(voyage));
    }

    public VoyageResponseDTO updateVoyage(
            Long id, VoyageRequestDTO dto) {

        Voyage voyage = findVoyage(id);
        updateEntity(voyage, dto);
        return toDTO(voyageRepository.save(voyage));
    }

    public void deleteVoyage(Long id) {
        Voyage voyage = findVoyage(id);
        voyageRepository.delete(voyage);
    }

    private Voyage findVoyage(Long id) {
        return voyageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Voyage introuvable : " + id
                ));
    }

    private void updateEntity(Voyage voyage, VoyageRequestDTO dto) {
        voyage.setDestination(dto.destination());
        voyage.setDescription(dto.description());
        voyage.setDateDepart(dto.dateDepart());
        voyage.setDateRetour(dto.dateRetour());
        voyage.setPrix(dto.prix());
    }

    private VoyageResponseDTO toDTO(Voyage voyage) {
        return new VoyageResponseDTO(
                voyage.getId(),
                voyage.getDestination(),
                voyage.getDescription(),
                voyage.getDateDepart(),
                voyage.getDateRetour(),
                voyage.getPrix()
        );
    }
}
