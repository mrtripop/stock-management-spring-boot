package com.mrtripop.inventory.repository;

import com.mrtripop.inventory.models.db.DigitalSignature;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, Long> {

  Optional<DigitalSignature> findByStoreStockId(Long storeStockId);
}
