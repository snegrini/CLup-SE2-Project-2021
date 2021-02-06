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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    public void setUp() {
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
    public void addTicket_SuccessfulAdd_InputValid() throws BadTicketException, BadStoreException, BadOpeningHourException {
        String customerId = "aaaa";

        TicketEntity t1 = new TicketEntity();
        t1.setQueueNumber(1);
        t1.setArrivalTime(new Time(1610000000000L)); // Jan 07 2021 06:13:20

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
    public void addTicket_FailAdd_InvalidStore() {
        String customerId = "aaaa";

        when(em.find(eq(StoreEntity.class), any())).thenReturn(null);
        assertThrows(BadStoreException.class, () -> ticketService.addTicket(customerId, store1.getStoreId()));
    }

    @Test
    public void addTicket_FailAdd_GotAlreadyTicket() {
        String customerId = "aaaa";

        TicketEntity t1 = new TicketEntity();

        when(em.find(eq(StoreEntity.class), any())).thenReturn(store1);
        when(em.createNamedQuery(eq("TicketEntity.findByCustomerIdOnDay"), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(BadTicketException.class, () -> ticketService.addTicket(customerId, store1.getStoreId()));
    }

    @Test
    public void addTicket_FailAdd_StoreClosed() throws BadOpeningHourException {
        String customerId = "aaaa";

        TicketEntity t1 = new TicketEntity();
        t1.setQueueNumber(1);
        t1.setArrivalTime(new Time(1610000000000L)); // Jan 07 2021 06:13:20

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
    public void deleteTicket_CustomerSuccessfulDelete_TicketValid() {
        String customerId = "aaaa";
        int ticketId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setCustomerId(customerId);
        t1.setTicketId(ticketId);
        store1.addTicket(t1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);

        assertDoesNotThrow(() -> ticketService.deleteTicket(customerId, ticketId));
    }

    @Test
    public void deleteTicket_CustomerFailDelete_TicketNull() {
        String customerId = "aaaa";
        int ticketId = 1;

        when(em.find(eq(TicketEntity.class), any())).thenReturn(null);

        assertThrows(BadTicketException.class, () -> ticketService.deleteTicket(customerId, ticketId));
    }

    @Test
    public void deleteTicket_CustomerFailDelete_Unauthorized() {
        String customerId = "aaaa";
        int ticketId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setCustomerId(customerId);
        t1.setTicketId(ticketId);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(customerId + "A", ticketId));
    }

    @Test
    public void deleteTicket_ManagerSuccessfulDelete_TicketValid() {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        store1.addTicket(t1);

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.MANAGER);
        store1.addUser(u1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertDoesNotThrow(() -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_UserNull() {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        store1.addTicket(t1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_TicketNull() {
        int ticketId = 1;
        int userId = 1;

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.MANAGER);
        store1.addUser(u1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(null);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(BadTicketException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_UnauthorizedRole() {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        store1.addTicket(t1);

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.EMPLOYEE);
        store1.addUser(u1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    public void deleteTicket_ManagerFailDelete_UnauthorizedStore() {
        int ticketId = 1;
        int userId = 1;

        TicketEntity t1 = new TicketEntity();
        t1.setTicketId(ticketId);
        store1.addTicket(t1);

        UserEntity u1 = new UserEntity();
        u1.setUserId(userId);
        u1.setRole(UserRole.EMPLOYEE);
        store2.addUser(u1);

        when(em.find(eq(TicketEntity.class), any())).thenReturn(t1);
        when(em.find(eq(UserEntity.class), any())).thenReturn(u1);

        assertThrows(UnauthorizedException.class, () -> ticketService.deleteTicket(ticketId, userId));
    }

    @Test
    public void updateTicketStatus_SuccessfulUpdate_TicketValid() throws UnauthorizedException, BadTicketException, BadStoreException {
        String passCode = "AAA000";
        int customerInside = 5;

        store1.setCustomersInside(customerInside);

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setPassStatus(PassStatus.VALID);
        store1.addTicket(t1);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertEquals(PassStatus.USED, ticketService.updateTicketStatus(passCode, store1.getStoreId()));
        assertEquals(PassStatus.USED, t1.getPassStatus());
        assertEquals(customerInside + 1, store1.getCustomersInside());
    }

    @Test
    public void updateTicketStatus_SuccessfulUpdate_TicketUsed() throws UnauthorizedException, BadTicketException, BadStoreException {
        String passCode = "AAA000";
        int customerInside = 5;

        store1.setCustomersInside(customerInside);

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setPassStatus(PassStatus.USED);
        store1.addTicket(t1);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertEquals(PassStatus.EXPIRED, ticketService.updateTicketStatus(passCode, store1.getStoreId()));
        assertEquals(PassStatus.EXPIRED, t1.getPassStatus());
        assertEquals(customerInside - 1, store1.getCustomersInside());
    }

    @Test
    public void updateTicketStatus_SuccessfulUpdate_DefaultPassCode() throws UnauthorizedException, BadTicketException, BadStoreException {
        String passCode = "AAA000";
        int customerInside = 5;

        store1.setCustomersInside(customerInside);
        store1.setDefaultPassCode(passCode);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.empty());

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(store1);

        assertEquals(PassStatus.EXPIRED, ticketService.updateTicketStatus(passCode, store1.getStoreId()));
        assertEquals(customerInside - 1, store1.getCustomersInside());
    }

    @Test
    public void updateTicketStatus_FailUpdate_TicketExpired() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setPassStatus(PassStatus.EXPIRED);
        store1.addTicket(t1);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
        assertEquals(PassStatus.EXPIRED, t1.getPassStatus());
    }

    @Test
    public void updateTicketStatus_FailUpdate_TicketNull() {
        String passCode = "AAA000";
        store1.setDefaultPassCode("AAA001");

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(em.find(eq(StoreEntity.class), any())).thenReturn(store1);
        when(query1.getResultStream()).thenReturn(Stream.empty());

        assertThrows(BadTicketException.class, () -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
    }

    @Test
    public void updateTicketStatus_FailUpdate_Unauthorized() {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setPassStatus(PassStatus.VALID);
        store1.addTicket(t1);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(t1));

        assertThrows(UnauthorizedException.class, () -> ticketService.updateTicketStatus(passCode, store2.getStoreId()));
        assertEquals(PassStatus.VALID, t1.getPassStatus());
    }

    @Test
    public void updateTicketStatus_FailUpdate_InvalidStoreId() {
        String passCode = "AAA000";

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.empty());

        when(em.find(eq(StoreEntity.class), anyInt())).thenReturn(null);

        assertThrows(BadStoreException.class, () -> ticketService.updateTicketStatus(passCode, store1.getStoreId()));
    }

    @Test
    public void findValidStoreTickets_TicketFound() throws BadTicketException {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setDate(new Date(new java.util.Date().getTime()));
        t1.setArrivalTime(new Time(new java.util.Date().getTime()));
        t1.setPassStatus(PassStatus.VALID);
        store1.addTicket(t1);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(List.of(t1)));

        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(store1.getStoreId());
        assertEquals(List.of(t1), resultTickets);
    }

    @Test
    public void findValidStoreTickets_NoTicketFound_ExpiredDate() throws BadTicketException {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setDate(new Date(949097495)); // Friday, January 28, 2000 10:11:35 PM
        t1.setArrivalTime(new Time(new java.util.Date().getTime()));
        t1.setPassStatus(PassStatus.VALID);
        store1.addTicket(t1);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(List.of(t1)));

        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(store1.getStoreId());
        assertEquals(List.of(), resultTickets);
    }

    @Test
    public void findValidStoreTickets_NoTicketFound_ExpiredArrivalTime() throws BadTicketException {
        String passCode = "AAA000";

        TicketEntity t1 = new TicketEntity();
        t1.setPassCode(passCode);
        t1.setDate(new Date(new java.util.Date().getTime()));
        t1.setArrivalTime(new Time(new java.util.Date().getTime() - 900000 - 1000));
        t1.setPassStatus(PassStatus.VALID);
        store1.addTicket(t1);

        when(em.createNamedQuery(anyString(), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(new ArrayList<>(List.of(t1)));

        List<TicketEntity> resultTickets = ticketService.findValidStoreTickets(store1.getStoreId());
        assertEquals(List.of(), resultTickets);
    }
}