package com.travelbook.serviceavis.repository;



import com.travelbook.serviceavis.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    List<Avis> findByVoyageId(Long voyageId);
}
