package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpeningHourServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Object> query1;

    @InjectMocks
    private OpeningHourService ohService;

    private StoreEntity store;
    private OpeningHourEntity oh1;
    private OpeningHourEntity oh2;

    private Time from1;
    private Time to1;
    private Time from2;
    private Time to2;

    private static final int WEEK_DAY_MONDAY = 1; // Monday

    @BeforeEach
    void setUp() {
        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setMaxResults(anyInt())).thenReturn(query1);

        when(em.find(any(), anyInt())).thenReturn(null);
        when(em.merge(any())).thenReturn(null);

        store = new StoreEntity();

        store.setStoreId(1);
        store.setOpeningHours(new ArrayList<>());

        createOpeningHours();
    }

    private void createOpeningHours() {
        from1 = new Time(1612076400000L); // 08:00
        to1 = new Time(1612090800000L); // 12:00

        from2 = new Time(1612098000000L); // 14:00
        to2 = new Time(1612112400000L); // 18:00

        oh1 = new OpeningHourEntity();
        oh1.setWeekDay(WEEK_DAY_MONDAY);
        oh1.setFromTime(from1);
        oh1.setToTime(to1);
        oh1.setStore(store);

        oh2 = new OpeningHourEntity();
        oh2.setWeekDay(WEEK_DAY_MONDAY);
        oh2.setFromTime(from2);
        oh2.setToTime(to2);
        oh2.setStore(store);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addAllOpeningHour_SuccessfulAdd_InputValid() throws BadOpeningHourException {
        List<Time> fromTimeList = List.of(from1, from2);
        List<Time> toTimeList = List.of(to1, to2);

        ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store);

        assertEquals(List.of(oh1, oh2), store.getOpeningHours());
    }

    @Test
    void addAllOpeningHour_FailAdd_TimeOverlaps() {
        Time fromOverlap = new Time(1612083600000L); // 10:00

        List<Time> fromTimeList = List.of(from1, fromOverlap);
        List<Time> toTimeList = List.of(to1, to2);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store));
    }

    @Test
    void addAllOpeningHour_FailAdd_FromAfterTo() {
        List<Time> fromTimeList = List.of(to1);
        List<Time> toTimeList = List.of(from1);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store));
    }

    @Test
    void addAllOpeningHour_FailAdd_ToBorderline() {
        Time toBorderline = new Time(1612219800000L); // 23:50:00

        List<Time> fromTimeList = List.of(from1);
        List<Time> toTimeList = List.of(toBorderline);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store));
    }

    @Test
    void addAllOpeningHour_FailAdd_FromAndToBorderline() {
        Time fromBorderline = new Time(1612219800000L); // 23:50:00
        Time toBorderline = new Time(1612220100000L); // 23:55:00

        List<Time> fromTimeList = List.of(fromBorderline);
        List<Time> toTimeList = List.of(toBorderline);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store));
    }

    @Test
    void addAllOpeningHour_FailAdd_BadListOfTimes() {
        List<Time> fromTimeList = List.of(from1, from2);
        List<Time> toTimeList = List.of(to1);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store));
    }

    @Test
    void deleteAllOpeningHour_SuccessfulDelete() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(1);
        u1.setRole(UserRole.MANAGER);
        u1.setStore(store);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        assertDoesNotThrow(() -> ohService.deleteAllOpeningHour(ohList, u1.getUserId()));
    }

    @Test
    void deleteAllOpeningHour_FailDelete_UserNotFound() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(1);
        u1.setRole(UserRole.MANAGER);
        u1.setStore(store);

        when(em.find(eq(UserEntity.class), any())).thenReturn(null);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, u1.getUserId()));
    }

    @Test
    void deleteAllOpeningHour_FailDelete_OpeningHourNotFound() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(1);
        u1.setRole(UserRole.MANAGER);
        u1.setStore(store);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(null);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(null);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, u1.getUserId()));
    }

    @Test
    void deleteAllOpeningHour_FailDelete_UnauthorizedAccessDifferentStore() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        StoreEntity store2 = new StoreEntity();
        store2.setStoreId(2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(1);
        u1.setRole(UserRole.MANAGER);
        u1.setStore(store2);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, u1.getUserId()));
    }

    @Test
    void deleteAllOpeningHour_FailDelete_UnauthorizedAccessEmployee() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(1);
        u1.setRole(UserRole.EMPLOYEE);
        u1.setStore(store);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, u1.getUserId()));
    }

    @Test
    void isInOpeningHour_TimeValid_True() throws BadOpeningHourException {
        Time time = new Time(1612083600000L); // 10:00
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        store.setOpeningHours(ohList);

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store);
        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohList));

        assertTrue(ohService.isInOpeningHour(store.getStoreId(), time));
    }

    @Test
    void isInOpeningHour_TimeValid_False() throws BadOpeningHourException {
        Time time = new Time(1612094400000L); // 13:00
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        store.setOpeningHours(ohList);

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store);
        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohList));

        assertFalse(ohService.isInOpeningHour(store.getStoreId(), time));
    }

    @Test
    void isInOpeningHour_BadStore() throws BadOpeningHourException {
        Time time = new Time(1612094400000L); // 13:00
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        store.setOpeningHours(ohList);

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(null);

        assertThrows(BadOpeningHourException.class, () -> ohService.isInOpeningHour(store.getStoreId(), time));
    }
}