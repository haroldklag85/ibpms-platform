package com.ibpms.core.sac.repository;

import com.ibpms.core.sac.domain.SacMailbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SacMailboxRepository extends JpaRepository<SacMailbox, String> {
}
