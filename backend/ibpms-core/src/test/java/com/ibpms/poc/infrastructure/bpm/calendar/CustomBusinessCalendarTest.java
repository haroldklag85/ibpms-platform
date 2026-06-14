package com.ibpms.poc.infrastructure.bpm.calendar;

import com.ibpms.poc.infrastructure.jpa.entity.bpm.BusinessHoursEntity;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.BusinessHoursRepository;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.HolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomBusinessCalendarTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private BusinessHoursRepository businessHoursRepository;

    private CustomBusinessCalendar customBusinessCalendar;

    @BeforeEach
    void setUp() {
        customBusinessCalendar = new CustomBusinessCalendar(holidayRepository, businessHoursRepository);

        BusinessHoursEntity defaultHours = new BusinessHoursEntity();
        defaultHours.setStartTime(LocalTime.of(8, 0));
        defaultHours.setEndTime(LocalTime.of(17, 0));
        defaultHours.setWorkOnWeekends(false);
        defaultHours.setTimezone(ZoneId.systemDefault().getId());

        when(businessHoursRepository.findAll()).thenReturn(Collections.singletonList(defaultHours));
        when(holidayRepository.findAll()).thenReturn(Collections.emptyList());
    }

    private Date createBaseDate(int hour) {
        // Lunes, 12 de julio de 2026 (Un día hábil genérico)
        return Date.from(LocalDate.of(2026, 7, 13).atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void parseDuration_PT4H_returnsBasePlus4Hours() {
        Date base = createBaseDate(8); // 8 AM
        Date result = customBusinessCalendar.resolveDuedate("PT4H", base, 0);
        // Esperamos 8 + 4 = 12 AM
        assertThat(result.toInstant().atZone(ZoneId.systemDefault()).getHour()).isEqualTo(12);
    }

    @Test
    void parseDuration_PT8H_returnsNextDay() {
        Date base = createBaseDate(10); // 10 AM, +8 horas hábiles saltará a mañana
        Date result = customBusinessCalendar.resolveDuedate("PT8H", base, 0);
        assertThat(result.toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth()).isEqualTo(14); // Salta al día 14
    }

    @Test
    void parseDuration_P2D_resolvesCorrectly() {
        Date base = createBaseDate(8); 
        // P2D = 48 horas. Se debe iterar calculando por días hábiles.
        Date result = customBusinessCalendar.resolveDuedate("P2D", base, 0);
        assertThat(result).isAfter(base);
    }

    @Test
    void parseDuration_4hString_returnsBasePlus4Hours() {
        Date base = createBaseDate(8);
        Date result = customBusinessCalendar.resolveDuedate("4h", base, 0);
        assertThat(result.toInstant().atZone(ZoneId.systemDefault()).getHour()).isEqualTo(12);
    }
    
    @Test
    void parseDuration_8hString_returnsBasePlus8HoursAndSkipsDay() {
        Date base = createBaseDate(12); // 12 PM
        Date result = customBusinessCalendar.resolveDuedate("8h", base, 0);
        assertThat(result.toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth()).isEqualTo(14);
    }

    @Test
    void parseDuration_NumericString_returnsCorrectHours() {
        Date base = createBaseDate(8);
        Date result = customBusinessCalendar.resolveDuedate("2", base, 0);
        assertThat(result.toInstant().atZone(ZoneId.systemDefault()).getHour()).isEqualTo(10);
    }

    @Test
    void parseDuration_Garbage_returns4HourFallback() {
        Date base = createBaseDate(8);
        Date result = customBusinessCalendar.resolveDuedate("garbage", base, 0);
        // By default parse error uses 4 hours fallback
        assertThat(result.toInstant().atZone(ZoneId.systemDefault()).getHour()).isEqualTo(12);
    }

    @Test
    void parseDuration_Null_returns4HourFallback() {
        Date base = createBaseDate(8);
        Date result = customBusinessCalendar.resolveDuedate(null, base, 0);
        assertThat(result.toInstant().atZone(ZoneId.systemDefault()).getHour()).isEqualTo(12);
    }
}
