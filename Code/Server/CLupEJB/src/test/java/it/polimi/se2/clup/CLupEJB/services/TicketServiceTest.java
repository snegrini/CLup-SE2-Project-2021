package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Object> query;

    private StoreEntity store1;
    private StoreEntity store2;

    @BeforeEach
    void setUp() {
        when(em.createNamedQuery(anyString(), any())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        store1 = new StoreEntity();
        store2 = new StoreEntity();

        store1.setStoreId(1);
        store2.setStoreId(2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findStoreTickets() throws BadTicketException {
        TicketEntity t1 = new TicketEntity();
        t1.setStore(store1);

        TicketEntity t2 = new TicketEntity();
        t2.setStore(store1);

        when(query.getResultList()).thenReturn(List.of(t1, t2));

        List<TicketEntity> resultList = ticketService.findStoreTickets(1);
        assertNotNull(resultList);
        assertEquals(List.of(t1, t2), resultList);
    }

    @Test
    void getCustomersQueue() {
    }

    @Test
    void findCustomerTickets() {
    }

    @Test
    void updateTicketStatus() {
    }

    @Test
    void addTicket() {
    }

    @Test
    void deleteTicket() {
    }
}