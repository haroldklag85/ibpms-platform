package com.ibpms.poc.infrastructure.bpm.config;

import com.ibpms.poc.infrastructure.bpm.calendar.CustomBusinessCalendar;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.BusinessHoursRepository;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.HolidayRepository;
import org.camunda.bpm.engine.impl.calendar.MapBusinessCalendarManager;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Inyectador Arquitectónico de Camunda: Sustituye el CalendarManager base
 * alojando nuestro Resolutor SLA Híbrido en las claves "dueDate" y "duration".
 */
@Component
public class CamundaEngineConfiguration extends AbstractCamundaConfiguration {

    private final HolidayRepository holidayRepository;
    private final BusinessHoursRepository businessHoursRepository;

    public CamundaEngineConfiguration(HolidayRepository holidayRepository, BusinessHoursRepository businessHoursRepository) {
        this.holidayRepository = holidayRepository;
        this.businessHoursRepository = businessHoursRepository;
    }

    @Bean
    public CustomBusinessCalendar customBusinessCalendar() {
        return new CustomBusinessCalendar(holidayRepository, businessHoursRepository);
    }

    @Override
    public void preInit(org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration processEngineConfiguration) {
        CustomBusinessCalendar customCalendar = customBusinessCalendar();

        MapBusinessCalendarManager calendarManager = new MapBusinessCalendarManager();
        // Anclamos nuestro resolutor a las palabras clave reservadas del Engine
        calendarManager.addBusinessCalendar("dueDate", customCalendar);
        calendarManager.addBusinessCalendar("duration", customCalendar);
        calendarManager.addBusinessCalendar("cycle", customCalendar);
        
        processEngineConfiguration.setBusinessCalendarManager(calendarManager);
    }
}
