package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private OpeningHourService ohs;

    @Mock
    private TypedQuery<Object> query1;

    @Mock
    private TypedQuery<Object> query2;

    @Mock
    private TypedQuery<Object> query3;

    @Mock
    private TypedQuery<Object> query4;

    @InjectMocks
    private TicketService ticketService;

    private StoreEntity store1;
    private StoreEntity store2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setMaxResults(anyInt())).thenReturn(query1);

        when(query2.setParameter(anyString(), any())).thenReturn(query2);
        when(query2.setMaxResults(anyInt())).thenReturn(query2);

        when(query3.setParameter(anyString(), any())).thenReturn(query3);
        when(query3.setMaxResults(anyInt())).thenReturn(query3);

        when(query4.setParameter(anyString(), any())).thenReturn(query4);

        when(em.merge(any())).thenReturn(null);

        store1 = new StoreEntity();
        store2 = new StoreEntity();

        store1.setStoreId(1);
        store2.setStoreId(2);
    }

    @Test
    void addTicket_SuccessfulAdd_InputValid() throws BadTicketException, BadStoreException, BadOpeningHourException {
        String customerId = "aaaa";

        TicketEntity t1 = new TicketEntity();
        t1.setQueueNumber(1);
        t1.setArrivalTime(new Time(1610000000000L)); // Jan 07 2021 06:13:20

        store1.setTickets(new ArrayList<>( ));

        when(em.find(eq(StoreEntity.class), any())).thenReturn(store1);

        when(em.createNamedQuery(eq("TicketEntity.findByPassCode"), any())).thenReturn(query1);
        when(em.createNamedQuery(eq("TicketEntity.findByStoreSorted"), any())).thenReturn(query2);
        when(em.createNamedQuery(eq("TicketEntity.findByCustomerIdOnDay"), any())).thenReturn(query3);

        when(query1.getResultStream()).thenReturn(Stream.empty());
        when(query2.getResultStream()).thenReturn(Stream.of(t1));
        when(query3.getResultStream()).thenReturn(Stream.empty());

        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query4);
        when(query4.getResultList()).thenReturn(List.of());
        when(ohs.isInOpeningHour(anyInt(), any())).thenReturn(true);

        TicketEntity t2 = ticketService.addTicket(customerId, store1.getStoreId());
        assertEquals(store1, t2.getStore());
        assertEquals(PassStatus.VALID, t2.getPassStatus());
        assertEquals(2, t2.getQueueNumber());
    }

    @Test
    void addTicket_FailAdd_InvalidStore() {
        String customerId = "aaaa";

        when(em.find(eq(StoreEntity.class), any())).thenReturn(null);
        assertThrows(BadStoreException.class, () -> ticketService.addTicket(customerId, store1.getStoreId()));
    }

    @Test
    void addTicket_FailAdd_StoreClosed() throws BadOpeningHourException {
        String customerId = "aaaa";

        TicketEntity t1 = new TicketEntity();
        t1.setQueueNumber(1);
        t1.setArrivalTime(new Time(1610000000000L)); // Jan 07 2021 06:13:20

        store1.setTickets(new ArrayList<>( ));

        when(em.find(eq(StoreEntity.class), any())).thenReturn(store1);

        when(em.createNamedQuery(eq("TicketEntity.findByPassCode"), any())).thenReturn(query1);
        when(em.createNamedQuery(eq("TicketEntity.findByStoreSorted"), any())).thenReturn(query2);
        when(em.createNamedQuery(eq("TicketEntity.findByCustomerIdOnDay"), any())).thenReturn(query3);

        when(query1.getResultStream()).thenReturn(Stream.empty());
        when(query2.getResultStream()).thenReturn(Stream.of(t1));
        when(query3.getResultStream()).thenReturn(Stream.empty());

        when(em.createNamedQuery(eq("OpeningHourEntity.findByStoreIdAndWeekDay"), any())).thenReturn(query4);
        when(query4.getResultList()).thenReturn(List.of());
        when(ohs.isInOpeningHour(anyInt(), any())).thenReturn(false);
        assertThrows(BadOpeningHourException.class, () -> ticketService.addTicket(customerId, store1.getStoreId()));
    }

    @Test
    void deleteTicket_CustomerSuccessfulDelete_TicketValid() throws UnauthorizedException, BadTicketException {
        String customerId = "aaaa";
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setCustomerId(customerId);
        t1.setPassCode(passCode);
        t1.setStore(store1);
        store1.setTickets(new ArrayList<>(List.of(t1)));

        when(em.createNamedQuery(any(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertDoesNotThrow(() -> ticketService.deleteTicket(customerId, passCode));
    }

    @Test
    void deleteTicket_CustomerFailDelete_TicketNull() {
        String customerId = "aaaa";
        String passCode = "AAA000";

        when(em.createNamedQuery(any(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.empty());

        assertThrows(BadTicketException.class, () -> ticketService.deleteTicket(customerId, passCode));
    }

    @Test
    void deleteTicket_CustomerFailDelete_Unauthorized() {
        String customerId = "aaaa";
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setCustomerId(customerId);
        t1.setPassCode(passCode);

        when(em.createNamedQuery(any(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(customerId + "A", passCode));
    }

    @Test
    void deleteTicket_ManagerSuccessfulDelete_TicketValid() throws UnauthorizedException, BadTicketException {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        t1.setStore(store1);
        t1.setStore(store1);
        store1.setTickets(new ArrayList<>(List.of(t1)));

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.MANAGER);
        u1.setStore(store1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertDoesNotThrow(() -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    void deleteTicket_ManagerFailDelete_UserNull() {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        t1.setStore(store1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    void deleteTicket_ManagerFailDelete_TicketNull() {
        int ticketId = 1;
        int userId = 1;

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.MANAGER);
        u1.setStore(store1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(null);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(BadTicketException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    void deleteTicket_ManagerFailDelete_UnauthorizedRole() throws UnauthorizedException, BadTicketException {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        t1.setStore(store1);

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.EMPLOYEE);
        u1.setStore(store1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    void deleteTicket_ManagerFailDelete_UnauthorizedStore() throws UnauthorizedException, BadTicketException {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        t1.setStore(store1);

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.EMPLOYEE);
        u1.setStore(store2);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    void updateTicketStatus_SuccessfulUpdate_TicketValid() throws UnauthorizedException, BadTicketException, BadStoreException {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.VALID);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        ticketService.updateTicketStatus(passCode, store1.getStoreId());
        assertEquals(PassStatus.USED, t1.getPassStatus());
    }

    @Test
    void updateTicketStatus_SuccessfulUpdate_TicketUsed() throws UnauthorizedException, BadTicketException, BadStoreException {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.USED);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        ticketService.updateTicketStatus(passCode, store1.getStoreId());
        assertEquals(PassStatus.EXPIRED, t1.getPassStatus());
    }

    @Test
    void updateTicketStatus_FailedUpdate_TicketExpired() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.EXPIRED);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
        assertEquals(PassStatus.EXPIRED, t1.getPassStatus());
    }

    @Test
    void updateTicketStatus_FailedUpdate_TicketNull() {
        String passCode = "AAA000";
        store1.setDefaultPassCode("AAA001");

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(em.find(eq(StoreEntity.class), any())).thenReturn(store1);
        when(query1.getResultStream()).thenReturn(Stream.empty());

        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
    }

    @Test
    void updateTicketStatus_FailedUpdate_Unauthorized() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.VALID);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(UnauthorizedException.class, () -> ticketService.updateTicketStatus(passCode, store2.getStoreId()));
        assertEquals(PassStatus.VALID, t1.getPassStatus());
    }

    @Test
    void findValidStoreTickets_TicketFound() throws BadTicketException {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        //t1.setArrivalTime(new Time(08:00:00));
        t1.setStore(store1);
        t1.setPassStatus(PassStatus.VALID);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(List.of(t1));

        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(store1.getStoreId());
        assertEquals(List.of(t1), resultTickets);
    }
}