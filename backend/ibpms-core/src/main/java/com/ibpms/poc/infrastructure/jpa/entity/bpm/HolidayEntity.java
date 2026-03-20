package com.ibpms.poc.infrastructure.jpa.entity.bpm;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ibpms_holiday")
public class HolidayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "holiday_date", nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(name = "description", length = 200)
    private String description;

    public HolidayEntity() {}

    public HolidayEntity(LocalDate holidayDate, String description) {
        this.holidayDate = holidayDate;
        this.description = description;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
