package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpeningHourServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void HasOverlap_NoOverlap_ReturnFalse() {
        int weekDay = 1; // weekday 1 corresponds to Monday.

        OpeningHourEntity oh1 = new OpeningHourEntity();
        oh1.setFromTime(Time.valueOf("08:00:00"));
        oh1.setToTime(Time.valueOf("12:00:00"));
        oh1.setWeekDay(weekDay);

        OpeningHourEntity oh2 = new OpeningHourEntity();
        oh2.setFromTime(Time.valueOf("12:00:00"));
        oh2.setToTime(Time.valueOf("18:00:00"));
        oh2.setWeekDay(weekDay);

        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        OpeningHourService ohService = new OpeningHourService();

        assertFalse(ohService.hasOverlap(ohList));
    }

    @Test
    public void HasOverlap_Overlap_ReturnTrue() {
        int weekDay = 1; // weekday 1 corresponds to Monday.

        OpeningHourEntity oh1 = new OpeningHourEntity();
        oh1.setFromTime(Time.valueOf("08:00:00"));
        oh1.setToTime(Time.valueOf("14:00:00"));
        oh1.setWeekDay(weekDay);

        OpeningHourEntity oh2 = new OpeningHourEntity();
        oh2.setFromTime(Time.valueOf("12:00:00"));
        oh2.setToTime(Time.valueOf("18:00:00"));
        oh2.setWeekDay(weekDay);

        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        OpeningHourService ohService = new OpeningHourService();

        assertTrue(ohService.hasOverlap(ohList));
    }
}