package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Time;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OpeningHourIntegrationTest {

    private static final String USER_CODE = "TTT000";
    private static final String PASSWORD = "test_password";
    private static final String INVALID_USER_CODE = "test";
    private static final String INVALID_PASSWORD = "test_pss";

    private static int LAST_STORE_ID = 0;
    private static int LAST_MANAGER_ID = 0;

    private static final int WEEK_DAY_MONDAY = 1; // Monday

    private static final Time FROM_TIME_1 = new Time(1612076400000L); // 08:00
    private static final Time TO_TIME_1 = new Time(1612090800000L); // 12:00
    private static final Time FROM_TIME_2 = new Time(1612098000000L); // 14:00
    private static final Time TO_TIME_2 = new Time(1612112400000L); // 18:00
    private static final Time FROM_TIME_OVERLAPPING = new Time(1612083600000L); // 10:00
    private static final Time FROM_TIME_BORDERLINE = new Time(1612219800000L); // 23:50:00
    private static final Time TO_TIME_BORDERLINE = new Time(1612220100000L); // 23:55:00

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private OpeningHourService ohService;

    private OpeningHourEntity expectedOh1;
    private OpeningHourEntity expectedOh2;


    @BeforeAll
    public static void setUpBeforeClass() {
        emf = Persistence.createEntityManagerFactory("CLupEJB-testing");
    }

    @AfterAll
    public static void tearDownAfterClass() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
        ohService = new OpeningHourService(em);
        createTestData();
    }

    @AfterEach
    public void tearDown() {
        if (em != null) {
            removeTestData();
            em.close();
        }
    }

    private void createTestData() {
        StoreEntity store = new StoreEntity();

        UserEntity manager = new UserEntity();
        manager.setPassword(USER_CODE);
        manager.setPassword(PASSWORD);
        manager.setRole(UserRole.MANAGER);

        store.addUser(manager);

        em.getTransaction().begin();
        em.persist(store);
        em.flush();
        LAST_STORE_ID = store.getStoreId();
        LAST_MANAGER_ID = manager.getUserId();
        em.getTransaction().commit();

        // Initialise expected values.
        expectedOh1 = new OpeningHourEntity();
        expectedOh1.setWeekDay(WEEK_DAY_MONDAY);
        expectedOh1.setFromTime(FROM_TIME_1);
        expectedOh1.setToTime(TO_TIME_1);

        expectedOh2 = new OpeningHourEntity();
        expectedOh2.setWeekDay(WEEK_DAY_MONDAY);
        expectedOh2.setFromTime(FROM_TIME_2);
        expectedOh2.setToTime(TO_TIME_2);
    }

    private void removeTestData() {
        em.getTransaction().begin();

        // On CASCADE ALL, opening hours and users are deleted too.
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);
        if (store != null) {
            em.remove(store);
        }
        em.getTransaction().commit();
    }

    @Test
    public void addAllOpeningHour_SuccessfulAdd_InputValid() throws BadOpeningHourException, UnauthorizedException {
        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_2);
        List<Time> toTimeList = List.of(TO_TIME_1, TO_TIME_2);

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        em.getTransaction().begin();
        ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, LAST_MANAGER_ID);
        List<OpeningHourEntity> ohList = store.getOpeningHours();
        em.getTransaction().rollback();

        assertEquals(expectedOh1.getWeekDay(), ohList.get(0).getWeekDay());
        assertEquals(expectedOh2.getWeekDay(), ohList.get(1).getWeekDay());

        assertEquals(expectedOh1.getFromTime(), ohList.get(0).getFromTime());
        assertEquals(expectedOh2.getFromTime(), ohList.get(1).getFromTime());

        assertEquals(expectedOh1.getToTime(), ohList.get(0).getToTime());
        assertEquals(expectedOh2.getToTime(), ohList.get(1).getToTime());
    }

    @Test
    public void addAllOpeningHour_FailAdd_TimeOverlaps() {
        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_OVERLAPPING);
        List<Time> toTimeList = List.of(TO_TIME_1, TO_TIME_2);

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, LAST_MANAGER_ID));
    }

    @Test
    public void addAllOpeningHour_FailAdd_FromAfterTo() {
        List<Time> fromTimeList = List.of(TO_TIME_1);
        List<Time> toTimeList = List.of(FROM_TIME_1);

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, LAST_MANAGER_ID));
    }

    @Test
    public void addAllOpeningHour_FailAdd_ToBorderline() {
        List<Time> fromTimeList = List.of(FROM_TIME_1);
        List<Time> toTimeList = List.of(TO_TIME_BORDERLINE);

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, LAST_MANAGER_ID));
    }

    @Test
    public void addAllOpeningHour_FailAdd_FromAndToBorderline() {
        List<Time> fromTimeList = List.of(FROM_TIME_BORDERLINE);
        List<Time> toTimeList = List.of(TO_TIME_BORDERLINE);

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, LAST_MANAGER_ID));
    }

    @Test
    public void addAllOpeningHour_FailAdd_BadListOfTimes() {
        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_2);
        List<Time> toTimeList = List.of(TO_TIME_1);

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        assertThrows(BadOpeningHourException.class, () -> ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, LAST_MANAGER_ID));
    }

    @Test
    public void deleteAllOpeningHour_SuccessfulDelete() throws BadOpeningHourException, UnauthorizedException {
        List<Time> fromTimeList = List.of(FROM_TIME_1, FROM_TIME_2);
        List<Time> toTimeList = List.of(TO_TIME_1, TO_TIME_2);

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        em.getTransaction().begin();
        ohService.addAllOpeningHour(WEEK_DAY_MONDAY, fromTimeList, toTimeList, store, LAST_MANAGER_ID);
        List<OpeningHourEntity> ohList = store.getOpeningHours();
        em.getTransaction().commit();

        em.getTransaction().begin();
        assertDoesNotThrow(() -> ohService.deleteAllOpeningHour(ohList, LAST_MANAGER_ID));
        em.getTransaction().commit();
    }

    @Test
    public void updateAllOpeningHour_Successfull() throws BadOpeningHourException, UnauthorizedException {
        Map<Integer, List<Time>> ohFromMap = Map.of(WEEK_DAY_MONDAY, List.of(FROM_TIME_1, FROM_TIME_2));
        Map<Integer, List<Time>> ohToMap = Map.of(WEEK_DAY_MONDAY, List.of(TO_TIME_1, TO_TIME_2));

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        em.getTransaction().begin();
        ohService.updateAllOpeningHour(store.getStoreId(), ohFromMap, ohToMap, LAST_MANAGER_ID);
        List<OpeningHourEntity> ohList = store.getOpeningHours();
        em.getTransaction().commit();

        assertEquals(expectedOh1.getWeekDay(), ohList.get(0).getWeekDay());
        assertEquals(expectedOh2.getWeekDay(), ohList.get(1).getWeekDay());

        assertEquals(expectedOh1.getFromTime(), ohList.get(0).getFromTime());
        assertEquals(expectedOh2.getFromTime(), ohList.get(1).getFromTime());

        assertEquals(expectedOh1.getToTime(), ohList.get(0).getToTime());
        assertEquals(expectedOh2.getToTime(), ohList.get(1).getToTime());

        em.getTransaction().begin();
        assertDoesNotThrow(() -> ohService.deleteAllOpeningHour(ohList, LAST_MANAGER_ID));
        em.getTransaction().commit();
    }

}
