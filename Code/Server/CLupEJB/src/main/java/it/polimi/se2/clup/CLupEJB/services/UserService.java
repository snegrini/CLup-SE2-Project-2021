package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.CredentialsException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.util.*;

@Stateless
public class UserService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    public UserService() {
    }

    public UserService(EntityManager em) {
        this.em = em;
    }

    public UserEntity checkCredentials(String usercode, String password) throws CredentialsException, NonUniqueResultException {
        List<UserEntity> uList;

        try {
            uList = em.createNamedQuery("UserEntity.findByUserCode", UserEntity.class)
                    .setParameter("usercode", usercode)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new CredentialsException("Could not verify credentals.");
        }

        if (uList.isEmpty()) {
            return null;
        } else if (uList.size() == 1) {
            UserEntity user = uList.get(0);
            PasswordEncoder encoder = new BCryptPasswordEncoder();

            if (encoder.matches(password, user.getPassword())) {
                return user;
            }
            return null;
        }

        throw new NonUniqueResultException("More than one user registered with same credentials.");
    }

    /**
     * Builds a user linked to the specified store with the specified role.
     *
     * @param store the store whose credentials shall be generated.
     * @param userRole the role the user shall be added.
     * @return the created user.
     * @throws  CredentialsException if the user role is of type forbidden.
     */
    private UserEntity buildUser(StoreEntity store, UserRole userRole) throws CredentialsException, BadStoreException {

        if (userRole.equals(UserRole.ADMIN)) {
            throw new CredentialsException("Cannot create credentials of this type!");
        }
        if (store == null) {
            throw new BadStoreException("Invalid store!");
        }

        UserEntity user = new UserEntity();

        String usercode = generateUsercode();
        String password = generatePassword();

        user.setUsercode(usercode);
        user.setPassword(password);
        user.setRole(userRole);
        user.setStore(store);

        return user;
    }

    private String generateUsercode() {
        String usercode;

        UserEntity collisionUser;
        do {
            // Creating user code
            usercode = UUID.randomUUID().toString().substring(0, 6);

            // Checking pass code uniqueness
            collisionUser = em.createNamedQuery("UserEntity.findByUserCode", UserEntity.class)
                    .setParameter("usercode", usercode)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            // Checking user code uniqueness
        } while (collisionUser != null);

        return usercode;
    }

    private String generatePassword() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    /**
     * Generates manager and employee credentials for the specified store.
     *
     * @param storeId the store id whose credentials shall be generated.
     * @param userId the user id who is performing this operation.
     * @return a list of user entity which contains the new created users.
     * @throws BadStoreException if store is not found.
     */
    public List<Map.Entry<String, String>> generateCredentials(int storeId, int userId) throws BadStoreException, UnauthorizedException {

        UserEntity user = em.find(UserEntity.class, userId);
        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadStoreException("Cannot load store.");
        }

        // Check user permissions.
        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Unauthorized operation.");
        }

        // Create users.
        UserEntity manager;
        UserEntity employee;
        try {
            manager = buildUser(store, UserRole.MANAGER);
            employee = buildUser(store, UserRole.EMPLOYEE);
        } catch (CredentialsException e) {
            throw new BadStoreException("Could not generate credentials.");
        }

        String managerPassword = manager.getPassword();
        String employeePassword = employee.getPassword();

        // After sending the email, hash the password to be stored in the DB.
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        manager.setPassword(encoder.encode(managerPassword));
        employee.setPassword(encoder.encode(employeePassword));

        // Add users to the store
        store.addUser(manager);
        store.addUser(employee);

        em.persist(store);

        List<Map.Entry<String, String>> users = new ArrayList<>();
        users.add(new AbstractMap.SimpleEntry<>(manager.getUsercode(), managerPassword));
        users.add(new AbstractMap.SimpleEntry<>(employee.getUsercode(), employeePassword));
        return users;
    }

}
