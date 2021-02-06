package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.*;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import it.polimi.se2.clup.CLupEJB.services.TicketService;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class TicketIntegrationTest {
    private static final String INIT_PASS_CODE = "test_init_pass_code";
    private static final int INIT_TICKET_QUEUE_NUMBER = 1;
    private static final String INIT_CUSTOMER_ID = "aaaa";

    private static final String PASS_CODE = "test_pass_code";
    private static final String CUSTOMER_ID = "bbbb";
    private static final int TICKET_QUEUE_NUMBER = 2;

    private static final String INVALID_PASS_CODE = "invalid_pass_code";

    private static final String USER_CODE_MANAGER = "MMM000";
    private static final String USER_CODE_EMPLOYEE = "EEE000";

    private static final String DEFAULT_STORE_NAME = "Default Store";
    private static final String DEFAULT_PEC = "defaultemail@pec.it";
    private static final String DEFAULT_PHONE = "000000000";
    private static final String DEFAULT_IMAGE_PATH = "defaultlogo.png";
    private static final String STORE_DEFAULT_PASS_CODE = "test_default_pass_code";
    private static final int DEFAULT_STORE_CAP = 50;
    private static final int DEFAULT_CUSTOMERS_INSIDE = 0;

    private static final int WEEK_DAY_MONDAY = 1; // Monday

    private static final Time FROM_TIME_1 = new Time(1612076400000L); // 08:00
    private static final Time TO_TIME_1 = new Time(1612090800000L); // 12:00

    private static int LAST_TICKET_ID = 0;
    private static int LAST_STORE_ID = 0;
    private static int LAST_MANAGER_ID = 0;
    private static int LAST_EMPLOYEE_ID = 0;

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private TicketService ticketService;

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
    void setUp() {
        em = emf.createEntityManager();
        OpeningHourService ohService = new OpeningHourService(em);
        ticketService = new TicketService(em, ohService);
        createTestData();
    }

    @AfterEach
    void tearDown() {
        if (em != null) {
            removeTestData();
            em.close();
        }
    }

    private void createTestData() {
        // Create a new store.
        StoreEntity store = new StoreEntity();
        store.setStoreName(DEFAULT_STORE_NAME);
        store.setPecEmail(DEFAULT_PEC);
        store.setPhone(DEFAULT_PHONE);
        store.setImagePath(DEFAULT_IMAGE_PATH);
        store.setDefaultPassCode(STORE_DEFAULT_PASS_CODE);
        store.setStoreCap(DEFAULT_STORE_CAP);
        store.setCustomersInside(DEFAULT_CUSTOMERS_INSIDE);
        store.setAddress(new AddressEntity());

        // Create users for store.
        UserEntity manager = new UserEntity();
        manager.setUsercode(USER_CODE_MANAGER);
        manager.setRole(UserRole.MANAGER);

        UserEntity employee = new UserEntity();
        employee.setUsercode(USER_CODE_EMPLOYEE);
        employee.setRole(UserRole.EMPLOYEE);

        store.addUser(manager);
        store.addUser(employee);

        // Create a new ticket.
        TicketEntity ticket = new TicketEntity();
        ticket.setPassCode(INIT_PASS_CODE);
        ticket.setCustomerId(INIT_CUSTOMER_ID);
        ticket.setDate(new Date(new java.util.Date().getTime()));

        ticket.setArrivalTime(new Time(new java.util.Date().getTime()));
        ticket.setPassStatus(PassStatus.VALID);
        ticket.setQueueNumber(INIT_TICKET_QUEUE_NUMBER);
        store.addTicket(ticket);

        // Persist data.
        em.getTransaction().begin();

        em.persist(store);
        em.flush();

        // Saving ID generated from SQL after the persist.
        LAST_TICKET_ID = ticket.getTicketId();
        LAST_STORE_ID = store.getStoreId();
        LAST_MANAGER_ID = manager.getUserId();
        LAST_EMPLOYEE_ID = employee.getUserId();

        em.getTransaction().commit();
    }

    private void removeTestData() {
        em.getTransaction().begin();

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        if (store != null) {
            em.remove(store);
        }

        em.getTransaction().commit();
    }

    @Test
    public void testValidGeneratedValueTicket_ValidPassCode() {
        TicketEntity ticket = ticketService.findValidTicketById(LAST_TICKET_ID);
        assertNotNull(ticket);
        assertEquals(INIT_PASS_CODE, ticket.getPassCode());
    }

    @Test
    public void testValidGeneratedValueTicket_InvalidPassCode() {
        TicketEntity ticket = ticketService.findValidTicketById(-1);
        assertNull(ticket);
    }

    @Test
    public void addTicket_SuccessfulAdd_InputValid() throws BadTicketException, BadStoreException, BadOpeningHourException {
        long timestamp = new java.util.Date().getTime();

        Time fromTime = Time.valueOf(new Time(timestamp - 7200000).toString()); // Two hours before now.
        Time toTime = Time.valueOf(new Time(timestamp + 7200000).toString()); // Two hours after now.

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(new SimpleDateFormat("EEEE", Locale.US).format(new java.util.Date()).toUpperCase());
        int weekDay = dayOfWeek.getValue(); // Today.

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        // Create opening hours.
        em.getTransaction().begin();
        OpeningHourEntity oh1 = new OpeningHourEntity();
        oh1.setWeekDay(weekDay);
        oh1.setFromTime(fromTime);
        oh1.setToTime(toTime);
        store.addOpeningHour(oh1);
        em.getTransaction().commit();

        TicketEntity t2 = ticketService.addTicket(CUSTOMER_ID, store.getStoreId());
        assertEquals(store, t2.getStore());
        assertEquals(PassStatus.VALID, t2.getPassStatus());
        assertEquals(TICKET_QUEUE_NUMBER, t2.getQueueNumber());
    }

    @Test
    public void addTicket_FailAdd_InvalidStore() {
        assertThrows(BadStoreException.class, () -> ticketService.addTicket(CUSTOMER_ID, -1));
    }

    @Test
    public void addTicket_FailAdd_GotAlreadyTicket() {
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);
        assertThrows(BadTicketException.class, () -> ticketService.addTicket(INIT_CUSTOMER_ID, store.getStoreId()));
    }

    @Test
    public void addTicket_FailAdd_StoreClosed() {
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);
        assertThrows(BadOpeningHourException.class, () -> ticketService.addTicket(CUSTOMER_ID, store.getStoreId()));
    }

    @Test
    public void deleteTicket_CustomerSuccessfulDelete_TicketValid() {
        em.getTransaction().begin();
        assertNotNull(ticketService.findValidTicketById(LAST_TICKET_ID));
        em.getTransaction().commit();

        em.getTransaction().begin();
        assertDoesNotThrow(() -> ticketService.deleteTicket(INIT_CUSTOMER_ID, LAST_TICKET_ID));
        assertNull(em.find(TicketEntity.class, LAST_TICKET_ID));
        em.getTransaction().commit();
    }

    @Test
    public void deleteTicket_CustomerFailDelete_TicketNull() {
        assertThrows(BadTicketException.class, () -> ticketService.deleteTicket(INIT_CUSTOMER_ID, -1));
    }

    @Test
    public void deleteTicket_CustomerFailDelete_Unauthorized() {
        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(INIT_CUSTOMER_ID + "A", LAST_TICKET_ID));
    }

    @Test
    public void deleteTicket_ManagerSuccessfulDelete_TicketValid() {
        assertDoesNotThrow(() -> ticketService.deleteTicket(LAST_TICKET_ID, LAST_MANAGER_ID));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_UserNull() {
        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(LAST_TICKET_ID, -1));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_TicketNull() {
        assertThrows(BadTicketException.class, () -> ticketService.deleteTicket(-1, LAST_MANAGER_ID));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_UnauthorizedRole() {
        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(LAST_TICKET_ID, LAST_EMPLOYEE_ID));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_UnauthorizedStore() {
        em.getTransaction().begin();

        // Create a new store with his manager user.
        StoreEntity store2 = new StoreEntity();
        store2.setStoreName(DEFAULT_STORE_NAME);
        store2.setPecEmail(DEFAULT_PEC);
        store2.setPhone(DEFAULT_PHONE);
        store2.setImagePath(DEFAULT_IMAGE_PATH);
        store2.setStoreCap(DEFAULT_STORE_CAP);
        store2.setCustomersInside(DEFAULT_CUSTOMERS_INSIDE);
        store2.setAddress(new AddressEntity());

        UserEntity manager2 = new UserEntity();
        manager2.setRole(UserRole.MANAGER);
        store2.addUser(manager2);

        em.persist(store2);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(LAST_TICKET_ID, manager2.getUserId()));

        em.getTransaction().commit();
    }

    @Test
    public void updateTicketStatus_SuccessfulUpdate_TicketValid() throws UnauthorizedException, BadTicketException, BadStoreException {
        TicketEntity ticket = em.find(TicketEntity.class, LAST_TICKET_ID);
        assertEquals(PassStatus.VALID, ticket.getPassStatus());

        assertEquals(PassStatus.USED, ticketService.updateTicketStatus(INIT_PASS_CODE, LAST_STORE_ID));
        assertEquals(PassStatus.USED, ticket.getPassStatus());

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);
        assertEquals(DEFAULT_CUSTOMERS_INSIDE + 1, store.getCustomersInside());
    }

    @Test
    public void updateTicketStatus_SuccessfulUpdate_TicketUsed() throws UnauthorizedException, BadTicketException, BadStoreException {
        em.getTransaction().begin();
        TicketEntity ticket = em.find(TicketEntity.class, LAST_TICKET_ID);
        ticket.setPassStatus(PassStatus.USED);
        em.merge(ticket);

        assertEquals(PassStatus.EXPIRED, ticketService.updateTicketStatus(INIT_PASS_CODE, LAST_STORE_ID));
        assertEquals(PassStatus.EXPIRED, ticket.getPassStatus());

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);
        em.getTransaction().commit();

        assertEquals(DEFAULT_CUSTOMERS_INSIDE - 1, store.getCustomersInside());
    }

    @Test
    public void updateTicketStatus_SuccessfulUpdate_DefaultPassCode() throws UnauthorizedException, BadTicketException, BadStoreException {
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        assertEquals(PassStatus.EXPIRED, ticketService.updateTicketStatus(STORE_DEFAULT_PASS_CODE, LAST_STORE_ID));
        assertEquals(DEFAULT_CUSTOMERS_INSIDE - 1, store.getCustomersInside());
    }

    @Test
    public void updateTicketStatus_FailUpdate_TicketExpired() {
        em.getTransaction().begin();
        TicketEntity ticket = em.find(TicketEntity.class, LAST_TICKET_ID);
        ticket.setPassStatus(PassStatus.EXPIRED);
        em.merge(ticket);


        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(INIT_PASS_CODE, LAST_STORE_ID));
        ticket = em.find(TicketEntity.class, LAST_TICKET_ID);
        em.getTransaction().commit();


        assertEquals(PassStatus.EXPIRED, ticket.getPassStatus());
    }

    @Test
    public void updateTicketStatus_FailUpdate_TicketNull() {
        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(INVALID_PASS_CODE, LAST_STORE_ID));
    }

    @Test
    public void updateTicketStatus_FailUpdate_Unauthorized() {

        em.getTransaction().begin();
        // Create a new store.
        StoreEntity store2 = new StoreEntity();
        store2.setStoreName(DEFAULT_STORE_NAME);
        store2.setPecEmail(DEFAULT_PEC);
        store2.setPhone(DEFAULT_PHONE);
        store2.setImagePath(DEFAULT_IMAGE_PATH);
        store2.setStoreCap(DEFAULT_STORE_CAP);
        store2.setCustomersInside(DEFAULT_CUSTOMERS_INSIDE);
        store2.setAddress(new AddressEntity());
        em.persist(store2);

        assertThrows(UnauthorizedException.class, () -> ticketService.updateTicketStatus(INIT_PASS_CODE, store2.getStoreId()));
        TicketEntity ticket = em.find(TicketEntity.class, LAST_TICKET_ID);
        em.getTransaction().commit();

        assertEquals(PassStatus.VALID, ticket.getPassStatus());
    }

    @Test
    public void updateTicketStatus_FailUpdate_InvalidStoreId() {
        assertThrows(BadStoreException.class, () -> ticketService.updateTicketStatus(INVALID_PASS_CODE, -1));
    }

    @Test
    public void findValidStoreTickets_TicketFound() throws BadTicketException {
        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(LAST_STORE_ID);

        assertNotNull(resultTickets);
        assertFalse(resultTickets.isEmpty());

        assertEquals(INIT_PASS_CODE, resultTickets.get(0).getPassCode());
    }

    @Test
    public void findValidStoreTickets_NoTicketFound_ExpiredDate() throws BadTicketException {
        em.getTransaction().begin();

        TicketEntity ticket = em.find(TicketEntity.class, LAST_TICKET_ID);
        ticket.setDate(new Date(949097495)); // Friday, January 28, 2000 10:11:35 PM
        em.merge(ticket);

        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(LAST_STORE_ID);
        em.getTransaction().commit();

        assertEquals(List.of(), resultTickets);
    }

    @Test
    public void findValidStoreTickets_NoTicketFound_ExpiredArrivalTime() throws BadTicketException {
        long timestamp = new java.util.Date().getTime();

        em.getTransaction().begin();
        TicketEntity ticket = em.find(TicketEntity.class, LAST_TICKET_ID);

        ticket.setArrivalTime(new Time(timestamp - 3600000));
        em.merge(ticket);

        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(LAST_STORE_ID);
        em.getTransaction().commit();

        assertEquals(List.of(), resultTickets);
    }

}
