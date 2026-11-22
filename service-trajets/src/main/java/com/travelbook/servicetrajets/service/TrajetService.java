package com.travelbook.servicetrajets.service;
import org.springframework.transaction.annotation.Transactional;
import com.travelbook.servicetrajets.dto.TrajetRequestDTO;
import com.travelbook.servicetrajets.dto.TrajetResponseDTO;
import com.travelbook.servicetrajets.entity.Trajet;
import com.travelbook.servicetrajets.repository.TrajetRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrajetService {

    private final TrajetRepository trajetRepository;

    public TrajetService(TrajetRepository trajetRepository) {
        this.trajetRepository = trajetRepository;
    }

    public List<TrajetResponseDTO> getAllTrajets() {
        return trajetRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TrajetResponseDTO getTrajetById(Long id) {
        return toDTO(findTrajet(id));
    }

    public List<TrajetResponseDTO> getTrajetsByVoyageId(Long voyageId) {
        return trajetRepository.findByVoyageId(voyageId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TrajetResponseDTO createTrajet(TrajetRequestDTO dto) {
        Trajet trajet = new Trajet();
        updateEntity(trajet, dto);

        return toDTO(trajetRepository.save(trajet));
    }

    public TrajetResponseDTO updateTrajet(
            Long id, TrajetRequestDTO dto) {

        Trajet trajet = findTrajet(id);
        updateEntity(trajet, dto);

        return toDTO(trajetRepository.save(trajet));
    }

    public void deleteTrajet(Long id) {
        Trajet trajet = findTrajet(id);
        trajetRepository.delete(trajet);
    }

    private Trajet findTrajet(Long id) {
        return trajetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trajet introuvable : " + id
                ));
    }

    private void updateEntity(Trajet trajet, TrajetRequestDTO dto) {
        trajet.setVoyageId(dto.voyageId());
        trajet.setVilleDepart(dto.villeDepart());
        trajet.setVilleArrivee(dto.villeArrivee());
        trajet.setDateHeureDepart(dto.dateHeureDepart());
        trajet.setDateHeureArrivee(dto.dateHeureArrivee());
        trajet.setMoyenTransport(dto.moyenTransport());
        trajet.setPrix(dto.prix());
        trajet.setPlacesRestantes(dto.placesRestantes());
    }

    private TrajetResponseDTO toDTO(Trajet trajet) {
        return new TrajetResponseDTO(
                trajet.getId(),
                trajet.getVoyageId(),
                trajet.getVilleDepart(),
                trajet.getVilleArrivee(),
                trajet.getDateHeureDepart(),
                trajet.getDateHeureArrivee(),
                trajet.getMoyenTransport(),
                trajet.getPrix(),
                trajet.getPlacesRestantes()
        );
    }
        @Transactional
    public void reserverPlaces(Long id, int nombre) {
        findTrajet(id);
        int modifie = trajetRepository.reserverPlaces(id, nombre);
        if (modifie == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pas assez de places disponibles pour le trajet " + id);
        }
    }

    @Transactional
    public void libererPlaces(Long id, int nombre) {
        findTrajet(id);
        trajetRepository.libererPlaces(id, nombre);
    }
}

