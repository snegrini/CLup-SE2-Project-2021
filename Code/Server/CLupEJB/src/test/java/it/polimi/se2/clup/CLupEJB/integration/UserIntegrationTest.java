package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.CredentialsException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.UserService;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserIntegrationTest {
    private static final String USER_CODE = "TTT000";
    private static final String PASSWORD = "test_password";
    private static final String INVALID_USER_CODE = "test";
    private static final String INVALID_PASSWORD = "test_pss";

    private static int LAST_USER_ID = 0;

    private static int LAST_STORE_ID = 0;

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private BCryptPasswordEncoder encoder;
    private UserService userService;

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
        encoder = new BCryptPasswordEncoder();

        userService = new UserService(em, encoder);
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
        UserEntity user = new UserEntity();
        user.setPassword(encoder.encode(PASSWORD));
        user.setUsercode(USER_CODE);
        user.setRole(UserRole.ADMIN);

        StoreEntity store = new StoreEntity();

        em.getTransaction().begin();
        em.persist(user);
        em.persist(store);
        em.flush();
        LAST_USER_ID = user.getUserId();
        LAST_STORE_ID = store.getStoreId();
        em.getTransaction().commit();
    }

    private void removeTestData() {
        em.getTransaction().begin();
        UserEntity user = em.find(UserEntity.class, LAST_USER_ID);
        if (user != null) {
            em.remove(user);
        }

        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);
        if (store != null) {
            em.remove(store);
        }
        em.getTransaction().commit();
    }

    @Test
    public void checkCredentials_ValidUser_CorrectPassword() throws CredentialsException {
        UserEntity user = userService.checkCredentials(USER_CODE, PASSWORD);

        assertNotNull(user);
        assertEquals(USER_CODE, user.getUsercode());
        assertTrue(encoder.matches(PASSWORD, user.getPassword()));
    }

    @Test
    public void checkCredentials_ValidUser_WrongPassword() throws CredentialsException {
        UserEntity user = userService.checkCredentials(USER_CODE, INVALID_PASSWORD);
        assertNull(user);
    }

    @Test
    public void checkCredentials_InvalidUser() throws CredentialsException {
        UserEntity user = userService.checkCredentials(INVALID_USER_CODE, PASSWORD);
        assertNull(user);
    }

    @Test
    public void generateCredentials_Authorized_Success() throws UnauthorizedException, BadStoreException, CredentialsException {
        UserEntity admin = em.find(UserEntity.class, LAST_USER_ID);
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        em.getTransaction().begin();
        List<Map.Entry<String, String>> genUsers = userService.generateCredentials(store, admin.getUserId());
        assertNotNull(genUsers);
        assertFalse(genUsers.isEmpty());
        assertFalse(genUsers.get(0).getKey().isEmpty());
        assertFalse(genUsers.get(0).getValue().isEmpty());
        assertFalse(genUsers.get(1).getKey().isEmpty());
        assertFalse(genUsers.get(1).getValue().isEmpty());

        assertEquals(genUsers.get(0).getKey(), userService.checkCredentials(genUsers.get(0).getKey(), genUsers.get(0).getValue()).getUsercode());
        assertEquals(genUsers.get(1).getKey(), userService.checkCredentials(genUsers.get(1).getKey(), genUsers.get(1).getValue()).getUsercode());

        em.getTransaction().rollback();
    }

    @Test
    public void generateCredentials_Unauthorized_Failure() throws UnauthorizedException, BadStoreException, CredentialsException {
        UserEntity admin = em.find(UserEntity.class, LAST_USER_ID);
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        em.getTransaction().begin();
        admin.setRole(UserRole.MANAGER);
        assertThrows(UnauthorizedException.class, () -> userService.generateCredentials(store, admin.getUserId()));
        em.getTransaction().rollback();
    }

    @Test
    public void generateCredentials_BadUser_Failure() {
        StoreEntity store = em.find(StoreEntity.class, LAST_STORE_ID);

        em.getTransaction().begin();
        assertThrows(UnauthorizedException.class, () -> userService.generateCredentials(store, -1));
        em.getTransaction().rollback();
    }

    @Test
    public void generateCredentials_BadStore_Failure() {
        UserEntity admin = em.find(UserEntity.class, LAST_USER_ID);

        em.getTransaction().begin();
        assertThrows(BadStoreException.class, () -> userService.generateCredentials(null, admin.getUserId()));
        em.getTransaction().rollback();
    }

}
