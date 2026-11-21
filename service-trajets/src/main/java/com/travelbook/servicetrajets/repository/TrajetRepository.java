package com.travelbook.servicetrajets.repository;


import com.travelbook.servicetrajets.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    List<Trajet> findByVoyageId(Long voyageId);
}
