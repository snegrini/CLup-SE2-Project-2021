package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.*;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import it.polimi.se2.clup.CLupEJB.services.UserService;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class StoreIntegrationTest {

    private static final String USER_CODE_ADMIN = "AAA000";
    private static final String USER_CODE_MANAGER = "MMM000";
    private static final String USER_CODE_EMPLOYEE = "EEE000";

    private static int LAST_STORE_ID = 0;
    private static int LAST_ADMIN_ID = 0;
    private static int LAST_MANAGER_ID = 0;
    private static int LAST_EMPLOYEE_ID = 0;

    private static final int WEEK_DAY_MONDAY = 1; // Monday

    private static final Time FROM_TIME_1 = new Time(1612076400000L); // 08:00
    private static final Time TO_TIME_1 = new Time(1612090800000L); // 12:00
    private static final Time FROM_TIME_2 = new Time(1612098000000L); // 14:00
    private static final Time TO_TIME_2 = new Time(1612112400000L); // 18:00

    private static final String DEFAULT_STORE_NAME = "Default Store";
    private static final String DEFAULT_PEC = "defaultemail@pec.it";
    private static final String DEFAULT_PHONE = "000000000";
    private static final String DEFAULT_IMAGE_PATH = "defaultlogo.png";
    private static final int DEFAULT_STORE_CAP = 50;
    private static final int DEFAULT_CUSTOMERS_INSIDE = 0;

    private static final String STORE_NAME = "Store";
    private static final String PEC = "email@pec.it";
    private static final String PHONE = "000000001";
    private static final String IMAGE_PATH = "logo.png";

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private StoreService storeService;

    private BCryptPasswordEncoder encoder;

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

        encoder = new BCryptPasswordEncoder();

        OpeningHourService ohService = new OpeningHourService(em);
        UserService userService = new UserService(em, encoder);
        storeService = new StoreService(em, ohService, userService);
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
        // Create CLup Admin user.
        UserEntity admin = new UserEntity();
        admin.setUsercode(USER_CODE_ADMIN);
        admin.setRole(UserRole.ADMIN);

        // Create a store with manager and employee users.
        StoreEntity store = new StoreEntity();
        store.setStoreName(DEFAULT_STORE_NAME);
        store.setPecEmail(DEFAULT_PEC);
        store.setPhone(DEFAULT_PHONE);
        store.setImagePath(DEFAULT_IMAGE_PATH);
        store.setStoreCap(DEFAULT_STORE_CAP);
        store.setCustomersInside(DEFAULT_CUSTOMERS_INSIDE);
        store.setAddress(new AddressEntity());
        store.setUsers(new ArrayList<>());

        UserEntity manager = new UserEntity();
        manager.setUsercode(USER_CODE_MANAGER);
        manager.setRole(UserRole.MANAGER);
        manager.setStore(store);

        UserEntity employee = new UserEntity();
        employee.setUsercode(USER_CODE_EMPLOYEE);
        employee.setRole(UserRole.EMPLOYEE);
        employee.setStore(store);

        store.addUser(manager);
        store.addUser(employee);

        em.getTransaction().begin();
        em.persist(admin);
        em.persist(store);
        em.flush();
        LAST_STORE_ID = store.getStoreId();
        LAST_ADMIN_ID = admin.getUserId();
        LAST_MANAGER_ID = manager.getUserId();
        LAST_EMPLOYEE_ID = employee.getUserId();
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
    public void addStore_SuccessfulAdd_InputValid() throws UnauthorizedException, BadStoreException {
        AddressEntity address = new AddressEntity();

        Map<Integer, List<Time>> ohFromMap = Map.of(WEEK_DAY_MONDAY, List.of(FROM_TIME_1, FROM_TIME_2));
        Map<Integer, List<Time>> ohToMap = Map.of(WEEK_DAY_MONDAY, List.of(TO_TIME_1, TO_TIME_2));

        em.getTransaction().begin();
        List<Map.Entry<String, String>> genUsers = storeService.addStore(STORE_NAME, PEC, PHONE, IMAGE_PATH, address, ohFromMap, ohToMap, LAST_ADMIN_ID);
        em.getTransaction().commit();

        assertNotNull(genUsers);
        assertFalse(genUsers.isEmpty());
        assertFalse(genUsers.get(0).getKey().isEmpty());
        assertFalse(genUsers.get(0).getValue().isEmpty());
        assertFalse(genUsers.get(1).getKey().isEmpty());
        assertFalse(genUsers.get(1).getValue().isEmpty());

        em.getTransaction().begin();
        assertNotNull(storeService.findStoreByName(STORE_NAME));
        assertNotNull(storeService.findStoreByPec(PEC));
        em.getTransaction().commit();
    }

    @Test
    public void addStore_FailAdd_NotUniqueName() {
        AddressEntity address = new AddressEntity();

        Map<Integer, List<Time>> ohFromMap = Map.of(WEEK_DAY_MONDAY, List.of(FROM_TIME_1, FROM_TIME_2));
        Map<Integer, List<Time>> ohToMap = Map.of(WEEK_DAY_MONDAY, List.of(TO_TIME_1, TO_TIME_2));

        em.getTransaction().begin();
        assertThrows(BadStoreException.class, () -> storeService.addStore(DEFAULT_STORE_NAME, PEC, PHONE, IMAGE_PATH, address, ohFromMap, ohToMap, LAST_ADMIN_ID));
        em.getTransaction().rollback();
    }

    @Test
    public void addStore_FailAdd_NotUniquePec() {
        AddressEntity address = new AddressEntity();

        Map<Integer, List<Time>> ohFromMap = Map.of(WEEK_DAY_MONDAY, List.of(FROM_TIME_1, FROM_TIME_2));
        Map<Integer, List<Time>> ohToMap = Map.of(WEEK_DAY_MONDAY, List.of(TO_TIME_1, TO_TIME_2));

        em.getTransaction().begin();
        assertThrows(BadStoreException.class, () -> storeService.addStore(STORE_NAME, DEFAULT_PEC, PHONE, IMAGE_PATH, address, ohFromMap, ohToMap, LAST_ADMIN_ID));
        em.getTransaction().rollback();
    }

    @Test
    public void addStore_FailAdd_InvalidOpeningHours() {
        AddressEntity address = new AddressEntity();

        Map<Integer, List<Time>> ohFromMap = Map.of(WEEK_DAY_MONDAY, List.of(FROM_TIME_1, FROM_TIME_2));
        Map<Integer, List<Time>> ohToMap = Map.of(WEEK_DAY_MONDAY, List.of(TO_TIME_1));

        em.getTransaction().begin();
        assertThrows(BadStoreException.class, () -> storeService.addStore(STORE_NAME, PEC, PHONE, IMAGE_PATH, address, ohFromMap, ohToMap, LAST_ADMIN_ID));
        em.getTransaction().rollback();
    }

    @Test
    public void addStore_FailAdd_UnauthorizedUser() {
        AddressEntity address = new AddressEntity();

        Map<Integer, List<Time>> ohFromMap = Map.of(WEEK_DAY_MONDAY, List.of(FROM_TIME_1, FROM_TIME_2));
        Map<Integer, List<Time>> ohToMap = Map.of(WEEK_DAY_MONDAY, List.of(TO_TIME_1, TO_TIME_2));

        em.getTransaction().begin();
        assertThrows(UnauthorizedException.class, () -> storeService.addStore(STORE_NAME, PEC, PHONE, IMAGE_PATH, address, ohFromMap, ohToMap, LAST_EMPLOYEE_ID));
        em.getTransaction().rollback();
    }

    @Test
    public void updateStoreCap_SuccessfulUpdate_InputValid() throws UnauthorizedException, BadStoreException {
        int storeCap = 45;

        em.getTransaction().begin();
        StoreEntity store = storeService.findStoreById(LAST_STORE_ID);
        storeService.updateStoreCap(storeCap, LAST_STORE_ID, LAST_MANAGER_ID);
        em.getTransaction().commit();

        assertEquals(storeCap, store.getStoreCap());
    }

    @Test
    public void updateStoreCap_FailUpdate_InvalidStore() {
        assertThrows(BadStoreException.class, () -> storeService.updateStoreCap(45, -1, LAST_MANAGER_ID));
    }

    @Test
    public void updateStoreCap_FailUpdate_InvalidUser() {
        assertThrows(BadStoreException.class, () -> storeService.updateStoreCap(45, LAST_STORE_ID, -1));
    }

    @Test
    public void updateStoreCap_FailUpdate_Unauthorized() {
        assertThrows(UnauthorizedException .class, () -> storeService.updateStoreCap(45, LAST_STORE_ID, LAST_EMPLOYEE_ID));
    }

    @Test
    public void getEstimateTime_SuccessfulEstimation_NotFullStore() throws BadStoreException {
        assertEquals(0, storeService.getEstimateTime(LAST_STORE_ID));
    }

    @Test
    public void getEstimateTime_SuccessfulEstimation_LastTicketCalled() throws BadStoreException {
        long timestamp = new java.util.Date().getTime();

        em.getTransaction().begin();
        StoreEntity store = storeService.findStoreById(LAST_STORE_ID);
        store.setCustomersInside(DEFAULT_STORE_CAP); // Filling the whole store.

        // Add new ticket to store.
        TicketEntity ticket = new TicketEntity();
        ticket.setArrivalTime(new Time(timestamp - 60000));
        ticket.setDate(new Date(timestamp));
        ticket.setStore(store);
        store.addTicket(ticket);

        em.persist(store);
        em.getTransaction().commit();

        assertEquals(15, storeService.getEstimateTime(LAST_STORE_ID));
    }

    @Test
    public void getEstimateTime_SuccessfulEstimation_AnotherTicketInQueue() throws BadStoreException {
        long timestamp = new java.util.Date().getTime();

        em.getTransaction().begin();
        StoreEntity store = storeService.findStoreById(LAST_STORE_ID);
        store.setCustomersInside(DEFAULT_STORE_CAP); // Filling the whole store.

        // Add new ticket to store.
        TicketEntity ticket = new TicketEntity();
        ticket.setArrivalTime(new Time(timestamp + 900000));
        ticket.setDate(new Date(timestamp));
        ticket.setStore(store);
        store.addTicket(ticket);

        em.persist(store);
        em.getTransaction().commit();

        assertEquals(29, storeService.getEstimateTime(LAST_STORE_ID));
    }

    @Test
    public void getEstimateTime_FailEstimation_InvalidStore() {
        assertThrows(BadStoreException.class, () -> storeService.getEstimateTime(-1));
    }
}
