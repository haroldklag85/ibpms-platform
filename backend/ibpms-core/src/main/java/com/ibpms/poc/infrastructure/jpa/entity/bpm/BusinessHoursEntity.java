package com.ibpms.poc.infrastructure.jpa.entity.bpm;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_business_hours")
public class BusinessHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime = LocalTime.of(8, 0);

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime = LocalTime.of(17, 0);

    @Column(name = "work_on_weekends", nullable = false)
    private Boolean workOnWeekends = false;

    @Column(name = "timezone", length = 50)
    private String timezone = "America/Bogota";

    public BusinessHoursEntity() {}

    public BusinessHoursEntity(LocalTime startTime, LocalTime endTime, Boolean workOnWeekends, String timezone) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.workOnWeekends = workOnWeekends;
        this.timezone = timezone != null ? timezone : "America/Bogota";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Boolean getWorkOnWeekends() { return workOnWeekends; }
    public void setWorkOnWeekends(Boolean workOnWeekends) { this.workOnWeekends = workOnWeekends; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
