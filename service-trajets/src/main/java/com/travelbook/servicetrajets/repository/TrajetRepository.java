package com.travelbook.servicetrajets.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.travelbook.servicetrajets.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    List<Trajet> findByVoyageId(Long voyageId);
        @Modifying
    @Query("UPDATE Trajet t SET t.placesRestantes = t.placesRestantes - :nombre "
         + "WHERE t.id = :id AND t.placesRestantes >= :nombre")
    int reserverPlaces(@Param("id") Long id, @Param("nombre") int nombre);

    @Modifying
    @Query("UPDATE Trajet t SET t.placesRestantes = t.placesRestantes + :nombre WHERE t.id = :id")
    int libererPlaces(@Param("id") Long id, @Param("nombre") int nombre);
}
