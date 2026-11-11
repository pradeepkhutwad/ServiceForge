package com.serviceforge.service;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TechnicianAvailabilityServiceTest {

    private MockDataStore dataStore;
    private TechnicianAvailabilityService service;

    @BeforeEach
    void setUp() {
        dataStore = new MockDataStore();
        dataStore.seed();
        service = new TechnicianAvailabilityService(dataStore);
    }

    @Test
    void booksAJobForAnAvailableTechnician() {
        LocalDateTime start = LocalDateTime.now().withHour(17).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        Job job = service.bookJob(1L, "New Customer", start, end);

        assertEquals("New Customer", job.getCustomerName());
        assertEquals(1L, job.getTechnicianId());
    }

    @Test
    void rejectsABookingThatOverlapsAnExistingJobWithADifferentStartTime() {
        LocalDateTime start = todayAt(10, 0);
        LocalDateTime end = todayAt(12, 0);

        assertThrows(IllegalStateException.class, () -> service.bookJob(1L, "Conflicting Customer", start, end));
    }

    @Test
    void rejectsABookingThatStartsBeforeTheTravelBufferAfterAnExistingJob() {
        LocalDateTime start = todayAt(11, 30);
        LocalDateTime end = todayAt(12, 30);

        assertThrows(IllegalStateException.class, () -> service.bookJob(1L, "Conflicting Customer", start, end));
    }

    @Test
    void rejectsABookingThatEndsTooCloseBeforeAnExistingJob() {
        LocalDateTime start = todayAt(7, 30);
        LocalDateTime end = todayAt(8, 30);

        assertThrows(IllegalStateException.class, () -> service.bookJob(1L, "Conflicting Customer", start, end));
    }

    @Test
    void allowsABookingWhenTheTravelBufferIsExactlySatisfied() {
        LocalDateTime start = todayAt(11, 45);
        LocalDateTime end = todayAt(12, 45);

        Job job = service.bookJob(1L, "New Customer", start, end);

        assertEquals("New Customer", job.getCustomerName());
    }

    @Test
    void allowsABookingBeforeAnExistingJobWhenTheTravelBufferIsExactlySatisfied() {
        LocalDateTime start = todayAt(8, 0);
        LocalDateTime end = todayAt(8, 15);

        Job job = service.bookJob(1L, "New Customer", start, end);

        assertEquals("New Customer", job.getCustomerName());
    }

    @Test
    void rejectsABookingAtAnIdenticalStartTimeAsAnExistingJob() {
        LocalDateTime start = todayAt(9, 0);
        LocalDateTime end = start.plusHours(1);

        assertThrows(IllegalStateException.class, () -> service.bookJob(1L, "Conflicting Customer", start, end));
    }

    @Test
    void throwsForAnUnknownTechnician() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);

        assertThrows(IllegalArgumentException.class, () -> service.bookJob(999L, "Nobody", start, end));
    }

    private LocalDateTime todayAt(int hour, int minute) {
        return LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0);
    }
}
