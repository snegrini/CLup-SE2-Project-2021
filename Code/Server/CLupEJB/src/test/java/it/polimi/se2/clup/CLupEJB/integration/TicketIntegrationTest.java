package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.services.TicketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import javax.naming.Context;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TicketIntegrationTest {
    private static final int TICKET_ID = 1;
    private static final String PASS_CODE = "test_pass_code";
    private static final String INVALID_PASS_CODE = "test_pss_cd";

    private EntityManagerFactory emf;
    private EntityManager em;

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.put(Context.PROVIDER_URL, "http://127.0.0.1:8081/tomee/ejb");

        emf = Persistence.createEntityManagerFactory("CLupEJB-testing", properties);
        em = emf.createEntityManager();
        createTestData();
    }

    @AfterEach
    void tearDown() {
        if (em != null) {
            removeTestData();
            em.close();
        }
        if (emf != null) {
            emf.close();
        }
    }

    private void createTestData() {
        TicketEntity ticket = new TicketEntity();
        ticket.setTicketId(TICKET_ID);
        ticket.setPassCode(PASS_CODE);

        em.getTransaction().begin();
        em.persist(ticket);
        em.getTransaction().commit();
    }

    private void removeTestData() {
        em.getTransaction().begin();
        TicketEntity ticket = em.find(TicketEntity.class, TICKET_ID);
        if (ticket != null) {
            em.remove(ticket);
        }
        em.getTransaction().commit();
    }

    @Test
    public void testValidGeneratedValueTicket_ValidPassCode() {
        TicketService ticketService = new TicketService(em);
        TicketEntity ticket = ticketService.findTicketById(1);
        assertNotNull(ticket);
        assertEquals(PASS_CODE, ticket.getPassCode());
    }

}
