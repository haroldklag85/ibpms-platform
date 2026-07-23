package com.ibpms.poc.infrastructure.bpm.calendar;

import com.ibpms.poc.infrastructure.jpa.entity.bpm.BusinessHoursEntity;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.BusinessHoursRepository;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.HolidayRepository;
import org.camunda.bpm.engine.impl.calendar.BusinessCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * CA-1, CA-2 y CA-4: Calendario de Negocios Híbrido.
 * Sobrescribe el cálculo UTC estricto 24/7 de Camunda pausando el reloj 
 * en fines de semana y días feriados, respetando el huso horario asignado.
 */
public class CustomBusinessCalendar implements BusinessCalendar {

    private static final Logger log = LoggerFactory.getLogger(CustomBusinessCalendar.class);
    private final HolidayRepository holidayRepository;
    private final BusinessHoursRepository businessHoursRepository;

    public CustomBusinessCalendar(HolidayRepository holidayRepository, BusinessHoursRepository businessHoursRepository) {
        this.holidayRepository = holidayRepository;
        this.businessHoursRepository = businessHoursRepository;
    }

    @Override
    public Date resolveDuedate(String duedateDescription) {
        return resolveDuedate(duedateDescription, new Date());
    }

    @Override
    public Date resolveDuedate(String duedateDescription, Date startDate) {
        return resolveDuedate(duedateDescription, startDate, 0L);
    }
    
    // CA-1 (SLA Matrix): Compatibilidad con abstracción de Camunda Engine
    public Date resolveDuedate(String duedateDescription, org.camunda.bpm.engine.task.Task task) {
        return resolveDuedate(duedateDescription, task.getCreateTime(), 0L);
    }
    
    /**
     * CA-1: Resolución principal de Due Dates con soporte ISO 8601.
     * CA-2: Bypass para timers sistémicos (SYSTEMIC_24_7).
     * CA-4: Resolución de timezone desde config corporativa persistida.
     * Soporta formatos: PT4H, P2D, Xh, número suelto. Fallback: 4h default.
     */
    public Date resolveDuedate(String duedateDescription, Date startDate, long repeatOffset) {
        log.info("[TIME-WARP] Resolviendo SLA: {} desde la fecha base: {}", duedateDescription, startDate);
        
        // 1. Obtener la Configuración Comercial
        List<BusinessHoursEntity> configs = businessHoursRepository.findAll();
        BusinessHoursEntity config = configs.isEmpty() ? new BusinessHoursEntity() : configs.get(0);
        
        // 2. Extraer Feriados Activos
        List<HolidayEntity> holidays = holidayRepository.findAll();
        
        // CA-2: Comprobación de Propiedad Sistémica (isBusinessSla)
        // En un entorno productivo completo se inyecta el Execution context para leer el BPMN Model Instance.
        // Si isBusinessSla == false, devolvemos el cálculo UTC nativo (Asumimos 24h a futuro para el stub)
        if (duedateDescription != null && duedateDescription.contains("SYSTEMIC_24_7")) {
             return new Date(startDate.getTime() + (24 * 3600 * 1000));
        }

        // SIMULADOR DE TIME-WARP PoC: 
        // Desplazamos la meta hasta encontrar horas hábiles.
        ZoneId zoneId = config.getTimezone() != null ? ZoneId.of(config.getTimezone()) : ZoneId.systemDefault();
        LocalDateTime metaTemporal = LocalDateTime.ofInstant(startDate.toInstant(), zoneId);
        
        // SLA parseando formato ISO 8601 o genéricos
        int horasSla = 4;
        try {
            if (duedateDescription != null) {
                if (duedateDescription.matches("^\\d+[hH]$")) {
                    horasSla = Integer.parseInt(duedateDescription.replaceAll("(?i)h", ""));
                } else if (duedateDescription.startsWith("P")) {
                    horasSla = (int) Duration.parse(duedateDescription).toHours();
                } else if (duedateDescription.matches("^\\d+$")) {
                    horasSla = Integer.parseInt(duedateDescription);
                }
            }
        } catch (Exception e) {
            log.warn("[TIME-WARP] Fallo parseando SLA '{}'. Usando fallback de {} horas.", duedateDescription, horasSla);
        }
        
        while (horasSla > 0) {
            metaTemporal = metaTemporal.plusHours(1);
            
            boolean esFeriado = isHoliday(metaTemporal, holidays);
            boolean esFinDeSemana = (metaTemporal.getDayOfWeek() == DayOfWeek.SATURDAY || metaTemporal.getDayOfWeek() == DayOfWeek.SUNDAY);
            
            boolean esFueraDeHorario = metaTemporal.toLocalTime().isBefore(config.getStartTime()) || 
                                       metaTemporal.toLocalTime().isAfter(config.getEndTime());
            
            if (!esFeriado && !(esFinDeSemana && !config.getWorkOnWeekends()) && !esFueraDeHorario) {
                horasSla--; // Descontamos hora solo si es hábil
            }
        }

        log.info("[TIME-WARP] SLA Resuelto protegiendo fines de semana/feriados. Nueva Fecha: {}", metaTemporal);
        return Date.from(metaTemporal.atZone(zoneId).toInstant());
    }

    private boolean isHoliday(LocalDateTime date, List<HolidayEntity> holidays) {
        return holidays.stream().anyMatch(h -> h.getHolidayDate().equals(date.toLocalDate()));
    }
}
