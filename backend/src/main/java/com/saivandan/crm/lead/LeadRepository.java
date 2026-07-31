package com.saivandan.crm.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
  Page<Lead> findByDeletedAtIsNull(Pageable pageable);
  Page<Lead> findByAssignedToIdAndDeletedAtIsNull(UUID userId, Pageable pageable);
  boolean existsByMobileAndDeletedAtIsNull(String mobile);
}

