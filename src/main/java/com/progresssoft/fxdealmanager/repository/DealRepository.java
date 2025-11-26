package com.progresssoft.fxdealmanager.repository;

import com.progresssoft.fxdealmanager.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, String> {
    
    boolean existsByUniqueId(String uniqueId);
}
