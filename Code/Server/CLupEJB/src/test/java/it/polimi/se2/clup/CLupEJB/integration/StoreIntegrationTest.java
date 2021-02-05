package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.AddressEntity;
import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import it.polimi.se2.clup.CLupEJB.services.UserService;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Time;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static final String STORE_NAME = "Store";
    private static final String PEC = "email@pec.it";
    private static final String PHONE = "000000000";
    private static final String IMAGE_PATH = "logo.png";

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private StoreService storeService;

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

        OpeningHourService ohService = new OpeningHourService();
        UserService userService = new UserService();
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

    private List<Map.Entry<String, String>> getCredentials() {
        return List.of(new AbstractMap.SimpleEntry<>("ManagerPasscode", "ManagerPassword"),
                new AbstractMap.SimpleEntry<>("EmployeePasscode", "EmployeePassword"));
    }

    @Test
    public void addStore_SuccessfulAdd_InputValid() throws UnauthorizedException, BadStoreException {
        AddressEntity address = new AddressEntity();

        Map<Integer, List<Time>> ohFromMap = Map.of(WEEK_DAY_MONDAY, List.of(FROM_TIME_1, FROM_TIME_2));
        Map<Integer, List<Time>> ohToMap = Map.of(WEEK_DAY_MONDAY, List.of(TO_TIME_1, TO_TIME_2));

        em.getTransaction().begin();
        storeService.addStore(STORE_NAME, PEC, PHONE, IMAGE_PATH, address, ohFromMap, ohToMap, LAST_ADMIN_ID);
        em.getTransaction().rollback();
    }

}
