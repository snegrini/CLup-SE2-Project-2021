package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.CredentialsException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Object> query1;

    @InjectMocks
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private StoreEntity store;

    private UserEntity user;
    private static final String USER_CODE = "TTT000";
    private static final String PASSWORD = "test_password";
    private static final String INVALID_USER_CODE = "test";
    private static final String INVALID_PASSWORD = "test_pss";


    @BeforeEach
    public void setUp() {
        when(query1.setParameter(anyString(), any())).thenReturn(query1);
        when(query1.setMaxResults(anyInt())).thenReturn(query1);

        when(em.merge(any())).thenReturn(null);

        user = new UserEntity();
        user.setUserId(1);
        user.setUsercode(USER_CODE);
        user.setPassword(PASSWORD);
        user.setRole(UserRole.ADMIN);
        store.addUser(user);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void checkCredentials_ValidUser_CorrectPassword() throws CredentialsException {
        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(List.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);

        UserEntity userChecked = userService.checkCredentials(USER_CODE, PASSWORD);

        assertNotNull(userChecked);
        assertEquals(USER_CODE, userChecked.getUsercode());
        assertEquals(PASSWORD, userChecked.getPassword());
    }

    @Test
    public void checkCredentials_ValidUser_WrongPassword() throws CredentialsException {
        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(List.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        UserEntity userChecked = userService.checkCredentials(USER_CODE, PASSWORD);

        assertNull(userChecked);
    }

    @Test
    public void checkCredentials_InvalidUser() throws CredentialsException {
        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(List.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        UserEntity userChecked = userService.checkCredentials(INVALID_USER_CODE, PASSWORD);

        assertNull(userChecked);
    }

    @Test
    public void checkCredentials_TwoUserSameCode_FailLogin() {
        // Create a second user with same credentials.
        UserEntity user2 = new UserEntity();
        user2.setPassword(PASSWORD);
        user2.setUsercode(USER_CODE);

        // Testing
        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);
        when(query1.getResultList()).thenReturn(List.of(user, user2));

        assertThrows(NonUniqueResultException.class, () -> userService.checkCredentials(USER_CODE, PASSWORD));
    }

    @Test
    public void generateCredentials_Authorized_Success() throws UnauthorizedException, BadStoreException {
        when(em.find(any(), eq(user.getUserId()))).thenReturn(user);

        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);

        Answer<Stream> answer = invocation -> Stream.empty();
        when(query1.getResultStream()).thenAnswer(answer);

        List<Map.Entry<String, String>> users = userService.generateCredentials(store, user.getUserId());
        for (Map.Entry<String, String> u : users) {
            assertNotNull(u.getKey());
            assertNotNull(u.getValue());
            assertFalse(u.getKey().isEmpty());
            assertFalse(u.getValue().isEmpty());
        }
    }

    @Test
    public void generateCredentials_NotAuthorized_Failure() {
        user.setRole(UserRole.EMPLOYEE);
        when(em.find(any(), eq(user.getUserId()))).thenReturn(user);

        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);

        assertThrows(UnauthorizedException.class, () -> userService.generateCredentials(store, user.getUserId()));
    }

    @Test
    public void generateCredentials_UserNull_Failure() {
        when(em.find(any(), eq(user.getUserId()))).thenReturn(null);

        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);

        assertThrows(UnauthorizedException.class, () -> userService.generateCredentials(store, user.getUserId()));
    }

    @Test
    public void generateCredentials_BadStore_Failure() {
        when(em.find(any(), eq(user.getUserId()))).thenReturn(user);

        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);

        assertThrows(BadStoreException.class, () -> userService.generateCredentials(null, user.getUserId()));
    }

    @Test
    public void regenerateCredentials_Authorized_Success() throws UnauthorizedException, BadStoreException, CredentialsException {
        when(em.find(any(), eq(user.getUserId()))).thenReturn(user);

        UserEntity manager = new UserEntity();
        manager.setRole(UserRole.MANAGER);
        manager.setUsercode("MMM001");
        manager.setPassword("passwordManager");

        UserEntity employee = new UserEntity();
        employee.setUsercode("EEE001");
        employee.setPassword("passwordEmployee");
        employee.setRole(UserRole.EMPLOYEE);

        when(store.getUsers()).thenReturn(List.of(manager, employee));

        List<UserEntity> users1 = List.of(manager, employee);

        List<Map.Entry<String, String>> users2 = userService.regenerateCredentials(store, user.getUserId());

        assertEquals(users1.size(), users2.size());

        for (int i = 0; i < users2.size(); i++) {
            UserEntity u1 = users1.get(i);
            Map.Entry<String, String> u2 = users2.get(i);

            assertNotNull(u2.getKey());
            assertNotNull(u2.getValue());
            assertFalse(u2.getKey().isEmpty());
            assertFalse(u2.getValue().isEmpty());
            assertEquals(u1.getUsercode(), u2.getKey());
            assertNotEquals(u1.getPassword(), u2.getValue());
        }
    }

    @Test
    public void regenerateCredentials_NotAuthorized_Failure() {
        user.setRole(UserRole.EMPLOYEE);
        when(em.find(any(), eq(user.getUserId()))).thenReturn(user);

        assertThrows(UnauthorizedException.class, () -> userService.regenerateCredentials(store, user.getUserId()));
    }

    @Test
    public void regenerateCredentials_UserNull_Failure() {
        when(em.find(any(), eq(user.getUserId()))).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> userService.regenerateCredentials(store, user.getUserId()));
    }

    @Test
    public void regenerateCredentials_BadStore_Failure() {
        when(em.find(any(), eq(user.getUserId()))).thenReturn(user);

        when(em.createNamedQuery(eq("UserEntity.findByUserCode"), any())).thenReturn(query1);

        assertThrows(BadStoreException.class, () -> userService.regenerateCredentials(null, user.getUserId()));
    }
}