package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.AddressEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
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
import java.sql.Time;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StoreServiceTest {
    @Mock
    private EntityManager em;

    @Mock
    private OpeningHourService ohs;

    @Mock
    private UserService us;

    @Mock
    private TypedQuery<Object> query1;

    @Mock
    private TypedQuery<Object> query2;

    @Mock
    private AddressEntity addressEntity;

    @Mock
    private Map<Integer, List<Time>> ohFromMap;

    @Mock
    private Map<Integer, List<Time>> ohToMap;

    @InjectMocks
    private StoreService storeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setMaxResults(anyInt())).thenReturn(query1);

        when(query2.setParameter(anyString(), any())).thenReturn(query2);
        when(query2.setMaxResults(anyInt())).thenReturn(query2);
    }

    private List<Map.Entry<String, String>> getCredentials() {
        return List.of(new AbstractMap.SimpleEntry<>("ManagerPasscode", "ManagerPassword"),
                new AbstractMap.SimpleEntry<>("EmployeePasscode", "EmployeePassword"));
    }

    @Test
    public void addStore_SuccessfulAdd_InputValid() throws BadOpeningHourException, UnauthorizedException, BadStoreException {
        String storeName = "Store";
        String pec = "email@pec.it";
        String phone = "000000000";
        String imagePath = "logo.png";
        int userId = 1;

        when(em.createNamedQuery(eq("StoreEntity.findByName"), any())).thenReturn(query1);
        when(em.createNamedQuery(eq("StoreEntity.findByPec"), any())).thenReturn(query2);

        when(query1.getResultStream()).thenReturn(Stream.empty());
        when(query2.getResultStream()).thenReturn(Stream.empty());

        doNothing().when(ohs).addAllOpeningHour(any(), anyMap(), anyMap(), anyInt());
        when(us.generateCredentials(any(), anyInt())).thenReturn(getCredentials());

        assertEquals(getCredentials(), storeService.addStore(storeName, pec, phone, imagePath, addressEntity, ohFromMap, ohToMap, userId));
    }

    @Test
    public void addStore_FailAdd_NotUniqueName() {
        String storeName = "Store";
        String pec = "email@pec.it";
        String phone = "000000000";
        String imagePath = "logo.png";
        int userId = 1;

        when(em.createNamedQuery(eq("StoreEntity.findByName"), any())).thenReturn(query1);

        when(query1.getResultStream()).thenReturn(Stream.of(new StoreEntity()));

        assertThrows(BadStoreException.class, () -> storeService.addStore(storeName, pec, phone, imagePath, addressEntity, ohFromMap, ohToMap, userId));
    }

    @Test
    public void addStore_FailAdd_NotUniquePec() {
        String storeName = "Store";
        String pec = "email@pec.it";
        String phone = "000000000";
        String imagePath = "logo.png";
        int userId = 1;

        when(em.createNamedQuery(eq("StoreEntity.findByName"), any())).thenReturn(query1);
        when(em.createNamedQuery(eq("StoreEntity.findByPec"), any())).thenReturn(query2);

        when(query1.getResultStream()).thenReturn(Stream.empty());
        when(query2.getResultStream()).thenReturn(Stream.of(new StoreEntity()));

        assertThrows(BadStoreException.class, () -> storeService.addStore(storeName, pec, phone, imagePath, addressEntity, ohFromMap, ohToMap, userId));
    }

    @Test
    public void addStore_FailAdd_InvalidOpeningHours() throws BadOpeningHourException, UnauthorizedException {
        String storeName = "Store";
        String pec = "email@pec.it";
        String phone = "000000000";
        String imagePath = "logo.png";
        int userId = 1;

        when(em.createNamedQuery(eq("StoreEntity.findByName"), any())).thenReturn(query1);
        when(em.createNamedQuery(eq("StoreEntity.findByPec"), any())).thenReturn(query2);

        when(query1.getResultStream()).thenReturn(Stream.empty());
        when(query2.getResultStream()).thenReturn(Stream.empty());

        doThrow(BadOpeningHourException.class).when(ohs).addAllOpeningHour(any(), anyMap(), anyMap(), anyInt());

        assertThrows(BadStoreException.class, () -> storeService.addStore(storeName, pec, phone, imagePath, addressEntity, ohFromMap, ohToMap, userId));
    }

    @Test
    public void addStore_FailAdd_UnauthorizedUser() throws BadOpeningHourException, UnauthorizedException, BadStoreException {
        String storeName = "Store";
        String pec = "email@pec.it";
        String phone = "000000000";
        String imagePath = "logo.png";
        int userId = 1;

        when(em.createNamedQuery(eq("StoreEntity.findByName"), any())).thenReturn(query1);
        when(em.createNamedQuery(eq("StoreEntity.findByPec"), any())).thenReturn(query2);

        when(query1.getResultStream()).thenReturn(Stream.empty());
        when(query2.getResultStream()).thenReturn(Stream.empty());

        doNothing().when(ohs).addAllOpeningHour(any(), anyMap(), anyMap(), anyInt());
        when(us.generateCredentials(any(), anyInt())).thenThrow(UnauthorizedException.class);

        assertThrows(BadStoreException.class, () -> storeService.addStore(storeName, pec, phone, imagePath, addressEntity, ohFromMap, ohToMap, userId));
    }

    @Test
    public void updateStoreCap_SuccessfulUpdate_InputValid() throws UnauthorizedException, BadStoreException {
        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setStoreId(1);
        storeEntity.setStoreCap(35);

        UserEntity userEntity = new UserEntity();
        userEntity.setRole(UserRole.MANAGER);
        storeEntity.addUser(userEntity);

        when(em.find(eq(StoreEntity.class), any())).thenReturn(storeEntity);
        when(em.find(eq(UserEntity.class), any())).thenReturn(userEntity);

        storeService.updateStoreCap(45, 1, 1);

        assertEquals(45, storeEntity.getStoreCap());
    }

    @Test
    public void updateStoreCap_FailUpdate_InvalidStore() {
        when(em.find(eq(StoreEntity.class), any())).thenReturn(null);

        assertThrows(BadStoreException.class, () -> storeService.updateStoreCap(45, 1, 1));
    }

    @Test
    public void updateStoreCap_FailUpdate_InvalidUser() {
        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setStoreId(1);
        storeEntity.setStoreCap(35);

        when(em.find(eq(StoreEntity.class), any())).thenReturn(storeEntity);
        when(em.find(eq(UserEntity.class), any())).thenReturn(null);

        assertThrows(BadStoreException.class, () -> storeService.updateStoreCap(45, 1, 1));
        assertEquals(35, storeEntity.getStoreCap());
    }

    @Test
    public void updateStoreCap_FailUpdate_NotManagerUser() {
        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setStoreId(1);
        storeEntity.setStoreCap(35);

        UserEntity userEntity = new UserEntity();
        userEntity.setRole(UserRole.EMPLOYEE);
        storeEntity.addUser(userEntity);

        when(em.find(eq(StoreEntity.class), any())).thenReturn(storeEntity);
        when(em.find(eq(UserEntity.class), any())).thenReturn(userEntity);

        assertThrows(UnauthorizedException.class, () -> storeService.updateStoreCap(45, 1, 1));

        assertEquals(35, storeEntity.getStoreCap());
    }

    @Test
    public void updateStoreCap_FailUpdate_NotManagerOfStore() {
        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setStoreId(1);
        storeEntity.setStoreCap(35);

        StoreEntity differentStore = new StoreEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setRole(UserRole.MANAGER);
        differentStore.addUser(userEntity);

        when(em.find(eq(StoreEntity.class), any())).thenReturn(storeEntity);
        when(em.find(eq(UserEntity.class), any())).thenReturn(userEntity);

        assertThrows(UnauthorizedException.class, () -> storeService.updateStoreCap(45, 1, 1));

        assertEquals(35, storeEntity.getStoreCap());
    }

    @Test
    public void getEstimateTime_SuccessfulEstimation_NotFullStore() throws BadStoreException {
        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setCustomersInside(15);
        storeEntity.setStoreCap(35);

        when(em.find(eq(StoreEntity.class), any())).thenReturn(storeEntity);
        assertEquals(0, storeService.getEstimateTime(1));
    }

    @Test
    public void getEstimateTime_SuccessfulEstimation_LastTicketCalled() throws BadStoreException {
        long timestamp = new java.util.Date().getTime();

        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setCustomersInside(35);
        storeEntity.setStoreCap(35);

        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setArrivalTime(new Time(timestamp - 60000));


        when(em.find(eq(StoreEntity.class), any())).thenReturn(storeEntity);

        when(em.createNamedQuery(eq("TicketEntity.findByStoreSorted"), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(ticketEntity));

        assertEquals(15, storeService.getEstimateTime(1));
    }

    @Test
    public void getEstimateTime_SuccessfulEstimation_AnotherTicketInQueue() throws BadStoreException {
        long timestamp = new java.util.Date().getTime();

        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setCustomersInside(35);
        storeEntity.setStoreCap(35);

        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setArrivalTime(new Time(timestamp + 900000));

        when(em.find(eq(StoreEntity.class), any())).thenReturn(storeEntity);

        when(em.createNamedQuery(eq("TicketEntity.findByStoreSorted"), any())).thenReturn(query1);
        when(query1.getResultStream()).thenReturn(Stream.of(ticketEntity));

        assertEquals(29, storeService.getEstimateTime(1));
    }

    @Test
    public void getEstimateTime_FailEstimation_InvalidStore() {
        when(em.find(eq(StoreEntity.class), any())).thenReturn(null);

        assertThrows(BadStoreException.class, () -> storeService.getEstimateTime(1));
    }
}
