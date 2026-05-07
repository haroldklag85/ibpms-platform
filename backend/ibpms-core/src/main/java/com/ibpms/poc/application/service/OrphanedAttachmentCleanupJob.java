package com.ibpms.poc.application.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrphanedAttachmentCleanupJob {

    private final JdbcTemplate jdbcTemplate;

    public OrphanedAttachmentCleanupJob(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 0 3 * * ?") // Diario a las 3 AM
    @Transactional
    public void cleanupOrphanedAttachments() {
        String sql = "DELETE FROM ibpms_orphaned_attachments WHERE orphaned_at < (NOW() - INTERVAL '24 HOURS')";
        int deletedRows = jdbcTemplate.update(sql);
        System.out.println("Orphaned attachments deleted: " + deletedRows);
    }
}
