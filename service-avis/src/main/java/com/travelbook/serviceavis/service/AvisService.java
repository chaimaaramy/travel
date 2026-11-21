package com.travelbook.serviceavis.service;

import com.travelbook.serviceavis.dto.AvisRequestDTO;
import com.travelbook.serviceavis.dto.AvisResponseDTO;
import com.travelbook.serviceavis.entity.Avis;
import com.travelbook.serviceavis.repository.AvisRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvisService {

    private final AvisRepository avisRepository;

    public AvisService(AvisRepository avisRepository) {
        this.avisRepository = avisRepository;
    }

    public List<AvisResponseDTO> getAllAvis() {
        return avisRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public AvisResponseDTO getAvisById(Long id) {
        return toDTO(findAvis(id));
    }

    public List<AvisResponseDTO> getAvisByVoyageId(Long voyageId) {
        return avisRepository.findByVoyageId(voyageId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public AvisResponseDTO createAvis(AvisRequestDTO dto) {
        Avis avis = new Avis();

        avis.setUserId(dto.userId());
        avis.setVoyageId(dto.voyageId());
        avis.setNote(dto.note());
        avis.setCommentaire(dto.commentaire());
        avis.setDateCreation(LocalDateTime.now());

        return toDTO(avisRepository.save(avis));
    }

    public AvisResponseDTO updateAvis(
            Long id, AvisRequestDTO dto) {

        Avis avis = findAvis(id);

        avis.setUserId(dto.userId());
        avis.setVoyageId(dto.voyageId());
        avis.setNote(dto.note());
        avis.setCommentaire(dto.commentaire());

        return toDTO(avisRepository.save(avis));
    }

    public void deleteAvis(Long id) {
        Avis avis = findAvis(id);
        avisRepository.delete(avis);
    }

    private Avis findAvis(Long id) {
        return avisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Avis introuvable : " + id
                ));
    }

    private AvisResponseDTO toDTO(Avis avis) {
        return new AvisResponseDTO(
                avis.getId(),
                avis.getUserId(),
                avis.getVoyageId(),
                avis.getNote(),
                avis.getCommentaire(),
                avis.getDateCreation()
        );
    }
}
