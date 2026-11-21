
package com.travelbook.servicepaiements.repository;

import com.travelbook.servicepaiements.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaiementRepository
        extends JpaRepository<Paiement, Long> {
}
