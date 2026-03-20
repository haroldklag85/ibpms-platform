package com.ibpms.poc.infrastructure.jpa.repository.bpm;

import com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<HolidayEntity, UUID> {
    Optional<HolidayEntity> findByHolidayDate(LocalDate date);
}
