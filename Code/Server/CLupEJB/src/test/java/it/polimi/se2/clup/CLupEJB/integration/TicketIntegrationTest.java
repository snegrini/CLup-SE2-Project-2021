package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.services.TicketService;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TicketIntegrationTest {
    private static final String PASS_CODE = "test_pass_code";
    private static final String INVALID_PASS_CODE = "test_pss_cd";
    private static final String STORE_NAME = "test_store_name";
    private static final int STORE_CAP = 10;

    private static int LAST_TICKET_ID = 0;
    private static int LAST_STORE_ID = 0;

    private static EntityManagerFactory emf;
    private static EntityManager em;

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
        store.setStoreName(STORE_NAME);
        store.setStoreCap(STORE_CAP);

        // Create a new ticket.
        TicketEntity ticket = new TicketEntity();
        ticket.setPassCode(PASS_CODE);
        ticket.setDate(new Date(new java.util.Date().getTime()));
        ticket.setArrivalTime(new Time(new java.util.Date().getTime()));
        ticket.setStore(store);
        ticket.setPassStatus(PassStatus.VALID);

        // Persist data.
        em.getTransaction().begin();

        em.persist(store);
        em.persist(ticket);
        em.flush();

        LAST_TICKET_ID = ticket.getTicketId();
        LAST_STORE_ID = store.getStoreId();

        em.getTransaction().commit();
    }

    private void removeTestData() {
        em.getTransaction().begin();

        TicketEntity ticket = em.find(TicketEntity.class, LAST_TICKET_ID);
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        if (ticket != null) {
            em.remove(ticket);
        }
        if (store != null) {
            em.remove(store);
        }

        em.getTransaction().commit();
    }

    @Test
    public void testValidGeneratedValueTicket_ValidPassCode() {
        TicketService ticketService = new TicketService(em);

        TicketEntity ticket = ticketService.findTicketById(LAST_TICKET_ID);
        assertNotNull(ticket);
        assertEquals(PASS_CODE, ticket.getPassCode());
    }

    @Test
    public void testValidGeneratedValueTicket_InvalidPassCode() {
        TicketService ticketService = new TicketService(em);
        TicketEntity ticket = ticketService.findTicketById(555);
        assertNull(ticket);
    }

    @Test
    void findValidStoreTickets_TicketFound() throws BadTicketException {
        TicketService ticketService = new TicketService(em);
        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(LAST_STORE_ID);

        assertNotNull(resultTickets);
        assertFalse(resultTickets.isEmpty());

        assertEquals(PASS_CODE, resultTickets.get(0).getPassCode());
    }

}
