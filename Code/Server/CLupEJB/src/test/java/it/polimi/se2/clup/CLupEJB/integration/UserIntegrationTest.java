package it.polimi.se2.clup.CLupEJB.integration;

import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.CredentialsException;
import it.polimi.se2.clup.CLupEJB.services.UserService;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

public class UserIntegrationTest {
    private static final String USER_CODE = "TTT000";
    private static final String PASSWORD = "test_password";
    private static final String INVALID_USER_CODE = "test";
    private static final String INVALID_PASSWORD = "test_pss";

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
        UserEntity user = new UserEntity();
        user.setPassword(PASSWORD);
        user.setUsercode(USER_CODE);

        em.getTransaction().begin();
        em.persist(user);
        em.flush();
        LAST_INSERT_ID = user.getUserId();
        em.getTransaction().commit();
    }

    private void removeTestData() {
        em.getTransaction().begin();
        UserEntity user = em.find(UserEntity.class, LAST_INSERT_ID);
        if (user != null) {
            em.remove(user);
        }
        em.getTransaction().commit();
    }

    @Test
    public void checkCredentials_ValidUser_CorrectPassword() throws CredentialsException {
        UserService userService = new UserService(em);
        UserEntity user = userService.checkCredentials(USER_CODE, PASSWORD);
        assertNotNull(user);
        assertEquals(USER_CODE, user.getUsercode());
        assertEquals(PASSWORD, user.getPassword());
    }

    @Test
    public void checkCredentials_ValidUser_WrongPassword() throws CredentialsException {
        UserService userService = new UserService(em);
        UserEntity user = userService.checkCredentials(USER_CODE, INVALID_PASSWORD);
        assertNull(user);
    }

    @Test
    public void checkCredentials_InvalidUser() throws CredentialsException {
        UserService userService = new UserService(em);
        UserEntity user = userService.checkCredentials(INVALID_USER_CODE, PASSWORD);
        assertNull(user);
    }

    @Test
    public void checkCredentials_TwoUserSameCode_FailLogin() {
        // Create a second user with same credentials.
        UserEntity user = new UserEntity();
        user.setPassword(PASSWORD);
        user.setUsercode(USER_CODE);

        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();

        // Testing
        UserService userService = new UserService(em);
        assertThrows(NonUniqueResultException.class, () -> userService.checkCredentials(USER_CODE, PASSWORD));

        // Clean up
        em.getTransaction().begin();
        em.remove(user);
        em.getTransaction().commit();
    }

}
