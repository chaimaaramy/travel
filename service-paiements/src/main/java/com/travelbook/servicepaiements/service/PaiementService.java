
package com.travelbook.servicepaiements.service;

import com.travelbook.servicepaiements.dto.PaiementRequestDTO;
import com.travelbook.servicepaiements.dto.PaiementResponseDTO;
import com.travelbook.servicepaiements.entity.Paiement;
import com.travelbook.servicepaiements.entity.StatutPaiement;
import com.travelbook.servicepaiements.repository.PaiementRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;

    public PaiementService(PaiementRepository paiementRepository) {
        this.paiementRepository = paiementRepository;
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

    public PaiementResponseDTO createPaiement(
            PaiementRequestDTO dto) {

        if (dto.statut() != StatutPaiement.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le paiement doit commencer EN_ATTENTE"
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

    public PaiementResponseDTO updatePaiement(
            Long id, PaiementRequestDTO dto) {

        Paiement paiement = findPaiement(id);

        paiement.setReservationId(dto.reservationId());
        paiement.setMontant(dto.montant());
        paiement.setModePaiement(dto.modePaiement());
        paiement.setStatut(dto.statut());

        return toDTO(paiementRepository.save(paiement));
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
