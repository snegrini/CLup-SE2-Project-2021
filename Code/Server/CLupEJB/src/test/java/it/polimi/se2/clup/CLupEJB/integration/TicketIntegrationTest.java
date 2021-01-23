package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.services.TicketService;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

public class TicketIntegrationTest {
    private static final String PASS_CODE = "test_pass_code";
    private static final String INVALID_PASS_CODE = "test_pss_cd";

    private static int LAST_INSERT_ID = 0;

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
        TicketEntity ticket = new TicketEntity();
        ticket.setPassCode(PASS_CODE);

        em.getTransaction().begin();
        em.persist(ticket);
        em.flush();
        LAST_INSERT_ID = ticket.getTicketId();
        em.getTransaction().commit();
    }

    private void removeTestData() {
        em.getTransaction().begin();
        TicketEntity ticket = em.find(TicketEntity.class, LAST_INSERT_ID);
        if (ticket != null) {
            em.remove(ticket);
        }
        em.getTransaction().commit();
    }

    @Test
    public void testValidGeneratedValueTicket_ValidPassCode() {
        TicketService ticketService = new TicketService(em);
        TicketEntity ticket = ticketService.findTicketById(LAST_INSERT_ID);
        assertNotNull(ticket);
        assertEquals(PASS_CODE, ticket.getPassCode());
    }

    @Test
    public void testValidGeneratedValueTicket_InvalidPassCode() {
        TicketService ticketService = new TicketService(em);
        TicketEntity ticket = ticketService.findTicketById(555);
        assertNull(ticket);
    }

}
