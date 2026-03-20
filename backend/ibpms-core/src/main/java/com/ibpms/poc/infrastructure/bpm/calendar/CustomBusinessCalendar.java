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
    
    // Fallback/Sobrecarga nativa en Camunda 7.20+ requiere a veces parametros extra pero estos 2 son obligatorios.
    // Camunda parsea internamente ISO-8601, pero en nuestra arquitectura abstraemos la resolución
    // Puesto que es una Prueba de Concepto (PoC) implementaremos un warp simbólico asumiendo +N horas de SLA
    // Aquí interceptamos el Time-Warp.
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
        LocalDateTime metaTemporal = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        
        // Sumamos arbitrariamente 4 horas hábiles por defecto al SLA si el formato es un genérico.
        // En V2 se implementará el parser ISO 8601 completo considerando JodaTime o java.time.Duration.
        int horasSla = 4; 
        
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
        return Date.from(metaTemporal.atZone(ZoneId.systemDefault()).toInstant());
    }

    private boolean isHoliday(LocalDateTime date, List<HolidayEntity> holidays) {
        return holidays.stream().anyMatch(h -> h.getHolidayDate().equals(date.toLocalDate()));
    }
}
