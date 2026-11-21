
package com.travelbook.servicevoyages.repository;

import com.travelbook.servicevoyages.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoyageRepository extends JpaRepository<Voyage, Long> {
}
