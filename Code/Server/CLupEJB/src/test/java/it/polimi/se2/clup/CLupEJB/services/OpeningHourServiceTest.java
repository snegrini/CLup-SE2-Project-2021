package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
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
import java.util.Map;

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
    private UserEntity manager;
    private OpeningHourEntity oh1;
    private OpeningHourEntity oh2;

    private static final Time FROM_TIME_1 = new Time(1612076400000L); // 08:00;
    private static final Time TO_TIME_1 = new Time(1612090800000L); // 12:00;
    private static final Time FROM_TIME_2 = new Time(1612098000000L); // 14:00;
    private static final Time TO_TIME_2 = new Time(1612112400000L); // 18:00;

    private static final String USER_CODE = "TTT000";
    private static final String PASSWORD = "test_password";

    private static final int WEEK_DAY_MONDAY = 1;
    private static final int WEEK_DAY_TUESDAY = 2;

    @BeforeEach
    public void setUp() {
        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setMaxResults(anyInt())).thenReturn(query1);

        when(em.find(any(), anyInt())).thenReturn(null);
        when(em.merge(any())).thenReturn(null);

        store = new StoreEntity();

        store.setStoreId(1);

        manager = new UserEntity();
        manager.setPassword(USER_CODE);
        manager.setPassword(PASSWORD);
        manager.setRole(UserRole.MANAGER);
        store.addUser(manager);

        createOpeningHours();
        store.addOpeningHour(oh1);
        store.addOpeningHour(oh2);
    }

    private void createOpeningHours() {
        oh1 = new OpeningHourEntity();
        oh1.setOpeningHoursId(1);
        oh1.setWeekDay(WEEK_DAY_MONDAY);
        oh1.setFromTime(FROM_TIME_1);
        oh1.setToTime(TO_TIME_1);

        oh2 = new OpeningHourEntity();
        oh2.setOpeningHoursId(2);
        oh2.setWeekDay(WEEK_DAY_MONDAY);
        oh2.setFromTime(FROM_TIME_2);
        oh2.setToTime(TO_TIME_2);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void addAllOpeningHour_SuccessfulAdd_InputValid() throws BadOpeningHourException, UnauthorizedException {

        // Create two new opening hours for late comparison.
        OpeningHourEntity oh3 = new OpeningHourEntity();
        oh3.setOpeningHoursId(1);
        oh3.setWeekDay(WEEK_DAY_TUESDAY);
        oh3.setFromTime(FROM_TIME_1);
        oh3.setToTime(TO_TIME_1);
        oh3.setStore(store);

        OpeningHourEntity oh4 = new OpeningHourEntity();
        oh4.setOpeningHoursId(2);
        oh4.setWeekDay(WEEK_DAY_TUESDAY);
        oh4.setFromTime(FROM_TIME_2);
        oh4.setToTime(TO_TIME_2);
        oh4.setStore(store);

        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_2);
        List<Time> toTimeList = List.of(TO_TIME_1, TO_TIME_2);

        when(em.find(eq(UserEntity.class), any())).thenReturn(manager);

        ohService.addAllOpeningHour(WEEK_DAY_TUESDAY, fromTimeList, toTimeList, store, manager.getUserId());

        List<OpeningHourEntity> expectedOhList = List.of(oh1, oh2, oh3, oh4);
        List<OpeningHourEntity> actualOhList= store.getOpeningHours();

        assertTrue(expectedOhList.size() == actualOhList.size() &&
                expectedOhList.containsAll(actualOhList) && actualOhList.containsAll(expectedOhList));
    }

    @Test
    public void addAllOpeningHour_FailAdd_TimeOverlaps() {
        Time fromOverlap = new Time(1612083600000L); // 10:00

        List<Time> fromTimeList = List.of(FROM_TIME_1, fromOverlap);
        List<Time> toTimeList = List.of(TO_TIME_1, TO_TIME_2);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, manager.getUserId()));
    }

    @Test
    public void addAllOpeningHour_FailAdd_FromAfterTo() {
        List<Time> fromTimeList = List.of(TO_TIME_1);
        List<Time> toTimeList = List.of(FROM_TIME_1);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, manager.getUserId()));
    }

    @Test
    public void addAllOpeningHour_FailAdd_ToBorderline() {
        Time toBorderline = new Time(1612219800000L); // 23:50:00

        List<Time> fromTimeList = List.of(FROM_TIME_1);
        List<Time> toTimeList = List.of(toBorderline);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, manager.getUserId()));
    }

    @Test
    public void addAllOpeningHour_FailAdd_FromAndToBorderline() {
        Time fromBorderline = new Time(1612219800000L); // 23:50:00
        Time toBorderline = new Time(1612220100000L); // 23:55:00

        List<Time> fromTimeList = List.of(fromBorderline);
        List<Time> toTimeList = List.of(toBorderline);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, manager.getUserId()));
    }

    @Test
    public void addAllOpeningHour_FailAdd_BadListOfTimes() {
        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_2);
        List<Time> toTimeList = List.of(TO_TIME_1);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, manager.getUserId()));
    }

    @Test
    public void addAllOpeningHour_FailAdd_UnauthorizedDifferentStore() {
        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_2);
        List<Time> toTimeList = List.of(TO_TIME_1, TO_TIME_2);

        StoreEntity store2 = new StoreEntity();
        store2.setStoreId(2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(2);
        u1.setRole(UserRole.MANAGER);
        store2.addUser(u1);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(UnauthorizedException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, u1.getUserId()));
    }

    @Test
    public void addAllOpeningHour_FailAdd_UnauthorizedEmployee() {
        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_2);
        List<Time> toTimeList = List.of(TO_TIME_1, TO_TIME_2);

        StoreEntity store2 = new StoreEntity();
        store2.setStoreId(2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(2);
        u1.setRole(UserRole.EMPLOYEE);
        store2.addUser(u1);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(UnauthorizedException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, u1.getUserId()));
    }

    @Test
    public void deleteAllOpeningHour_SuccessfulDelete() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        when(em.find(eq(UserEntity.class), any())).thenReturn(manager);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        assertDoesNotThrow(() -> ohService.deleteAllOpeningHour(ohList, manager.getUserId()));
    }

    @Test
    public void deleteAllOpeningHour_FailDelete_UserNotFound() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        when(em.find(eq(UserEntity.class), any())).thenReturn(manager);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, manager.getUserId()));
    }

    @Test
    public void deleteAllOpeningHour_FailDelete_OpeningHourNotFound() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        when(em.find(eq(UserEntity.class), any())).thenReturn(manager);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(null);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(null);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, manager.getUserId()));
    }

    @Test
    public void deleteAllOpeningHour_FailDelete_UnauthorizedAccessDifferentStore() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        StoreEntity store2 = new StoreEntity();
        store2.setStoreId(2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(2);
        u1.setRole(UserRole.MANAGER);
        store2.addUser(u1);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, u1.getUserId()));
    }

    @Test
    public void deleteAllOpeningHour_FailDelete_UnauthorizedAccessEmployee() {
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        UserEntity u1 = new UserEntity();
        u1.setUserId(2);
        u1.setRole(UserRole.EMPLOYEE);
        store.addUser(u1);

        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        assertThrows(BadOpeningHourException.class, () -> ohService.deleteAllOpeningHour(ohList, u1.getUserId()));
    }

    @Test
    public void updateAllOpeningHour_UpdateSuccessful() {
        Time fromTimeNew = new Time(1612083600000L); // 10:00
        List<OpeningHourEntity> ohListOld = List.of(oh1, oh2);

        Map<Integer, List<Time>> ohFromMap = Map.of(oh2.getWeekDay(), List.of(oh2.getFromTime(), fromTimeNew));
        Map<Integer, List<Time>> ohToMap = Map.of(oh2.getWeekDay(), List.of(oh2.getToTime(), TO_TIME_1));

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store);
        when(em.find(eq(UserEntity.class), anyInt())).thenReturn(manager);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohListOld));

        assertDoesNotThrow(() -> ohService.updateAllOpeningHour(store.getStoreId(), ohFromMap, ohToMap, manager.getUserId()));
    }

    @Test
    public void updateAllOpeningHour_UpdateFailed_BadUser() {
        Time fromTimeNew = new Time(1612083600000L); // 10:00
        List<OpeningHourEntity> ohListOld = List.of(oh1, oh2);

        // Create new opening hour.
        OpeningHourEntity oh3 = new OpeningHourEntity();
        oh3.setOpeningHoursId(3);
        oh3.setWeekDay(WEEK_DAY_MONDAY);
        oh3.setFromTime(fromTimeNew);
        oh3.setToTime(TO_TIME_1);
        store.addOpeningHour(oh3);

        assertEquals(oh2.getWeekDay(), oh3.getWeekDay());
        Map<Integer, List<Time>> ohFromMap = Map.of(oh2.getWeekDay(), List.of(oh2.getFromTime(), oh3.getFromTime()));
        Map<Integer, List<Time>> ohToMap = Map.of(oh2.getWeekDay(), List.of(oh2.getToTime(), oh3.getToTime()));

        UserEntity u1 = new UserEntity();
        u1.setUserId(1);
        u1.setRole(UserRole.MANAGER);
        u1.setStore(store);

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store);
        when(em.find(eq(UserEntity.class), anyInt())).thenReturn(null);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohListOld));

        assertThrows(BadOpeningHourException.class, () -> ohService.updateAllOpeningHour(store.getStoreId(), ohFromMap, ohToMap, 555));
    }

    @Test
    public void updateAllOpeningHour_UpdateFailed_BadStore() {
        Time fromTimeNew = new Time(1612083600000L); // 10:00
        List<OpeningHourEntity> ohListOld = List.of(oh1, oh2);

        // Create new opening hour.
        OpeningHourEntity oh3 = new OpeningHourEntity();
        oh3.setOpeningHoursId(3);
        oh3.setWeekDay(WEEK_DAY_MONDAY);
        oh3.setFromTime(fromTimeNew);
        oh3.setToTime(TO_TIME_1);
        store.addOpeningHour(oh3);

        assertEquals(oh2.getWeekDay(), oh3.getWeekDay());
        Map<Integer, List<Time>> ohFromMap = Map.of(oh2.getWeekDay(), List.of(oh2.getFromTime(), oh3.getFromTime()));
        Map<Integer, List<Time>> ohToMap = Map.of(oh2.getWeekDay(), List.of(oh2.getToTime(), oh3.getToTime()));

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(null);
        when(em.find(eq(UserEntity.class), anyInt())).thenReturn(manager);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohListOld));

        assertThrows(BadOpeningHourException.class, () -> ohService.updateAllOpeningHour(store.getStoreId(), ohFromMap, ohToMap, manager.getUserId()));
    }

    @Test
    public void updateAllOpeningHour_UpdateFailed_BadOpeningHourFormat() {
        Time fromTimeNew = new Time(1612083600000L); // 10:00
        List<OpeningHourEntity> ohListOld = List.of(oh1, oh2);

        // Create new opening hour.
        OpeningHourEntity oh3 = new OpeningHourEntity();
        oh3.setOpeningHoursId(3);
        oh3.setWeekDay(WEEK_DAY_MONDAY);
        oh3.setFromTime(fromTimeNew);
        oh3.setToTime(TO_TIME_1);
        store.addOpeningHour(oh3);

        assertEquals(oh2.getWeekDay(), oh3.getWeekDay());
        Map<Integer, List<Time>> ohFromMap = Map.of(oh2.getWeekDay(), List.of(oh2.getFromTime(), oh3.getFromTime()));
        Map<Integer, List<Time>> ohToMap = Map.of(oh2.getWeekDay(), List.of(oh2.getToTime()));

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store);
        when(em.find(eq(UserEntity.class), anyInt())).thenReturn(manager);
        when(em.find(OpeningHourEntity.class, oh1.getOpeningHoursId())).thenReturn(oh1);
        when(em.find(OpeningHourEntity.class, oh2.getOpeningHoursId())).thenReturn(oh2);

        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohListOld));

        assertThrows(BadOpeningHourException.class, () -> ohService.updateAllOpeningHour(store.getStoreId(), ohFromMap, ohToMap, manager.getUserId()));
    }

    @Test
    public void isInOpeningHour_TimeValid_True() throws BadOpeningHourException {
        Time time = new Time(1612083600000L); // 10:00
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store);
        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohList));

        assertTrue(ohService.isInOpeningHour(store.getStoreId(), time));
    }

    @Test
    public void isInOpeningHour_TimeValid_False() throws BadOpeningHourException {
        Time time = new Time(1612094400000L); // 13:00
        List<OpeningHourEntity> ohList = List.of(oh1, oh2);

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store);
        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(ohList));

        assertFalse(ohService.isInOpeningHour(store.getStoreId(), time));
    }

    @Test
    public void isInOpeningHour_BadStore() {
        Time time = new Time(1612094400000L); // 13:00

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(null);

        assertThrows(BadOpeningHourException.class, () -> ohService.isInOpeningHour(store.getStoreId(), time));
    }
}