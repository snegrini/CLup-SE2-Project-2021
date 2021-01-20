package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
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
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(em.merge(any())).thenReturn(null);

        store1 = new StoreEntity();
        store2 = new StoreEntity();

        store1.setStoreId(1);
        store2.setStoreId(2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findStoreTickets_ReturnTickets() throws BadTicketException {
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
    void updateTicketStatus_UpdateSuccessfull_TicketValid() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.VALID);

        when(query.getResultStream()).thenReturn(Stream.of(t1));

        assertDoesNotThrow(() -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
    }

    @Test
    void updateTicketStatus_UpdateSuccessfull_TicketUsed() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.USED);

        when(query.getResultStream()).thenReturn(Stream.of(t1));

        assertDoesNotThrow(() -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
    }

    @Test
    void updateTicketStatus_UpdateSuccessfull_TicketExpired() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.EXPIRED);

        when(query.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
    }

    @Test
    void updateTicketStatus_UpdateFailed_TicketNull() {
        String passCode = "AAA000";

        when(query.getResultStream()).thenReturn(Stream.empty());

        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
    }

    @Test
    void updateTicketStatus_UpdateFailed_Unauthorized() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.VALID);

        when(query.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(passCode, store2.getStoreId()));
    }


    @Test
    void addTicket() {
    }

    @Test
    void deleteTicket() {
    }
}