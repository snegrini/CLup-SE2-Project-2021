package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.CredentialsException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.UUID;

@Stateless
public class UserService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    public UserEntity checkCredentials(String usercode, String password) throws CredentialsException, NonUniqueResultException {
        List<UserEntity> uList = null;
        try {
            uList = em.createNamedQuery("UserEntity.checkCredentials", UserEntity.class)
                    .setParameter("usercode", usercode)
                    .setParameter("password", password)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new CredentialsException("Could not verify credentals.");
        }
        if (uList.isEmpty())
            return null;
        else if (uList.size() == 1)
            return uList.get(0);
        throw new NonUniqueResultException("More than one user registered with same credentials.");
    }

    /**
     * Adds a user to the specified store with the specified role.
     *
     * @param storeId the store id whose credentials shall be generated.
     * @param userRole the role the user shall be added.
     * @return the created user.
     * @throws  CredentialsException if the user role is of type forbidden.
     */
    public UserEntity addUser(int storeId, UserRole userRole) throws CredentialsException {

        if (userRole.equals(UserRole.ADMIN)) {
            throw new CredentialsException("Cannot create credentials of this type!");
        }

        UserEntity user = new UserEntity();

        String usercode = generateUsercode();
        String password = generatePassword();

        user.setUsercode(usercode);
        user.setPassword(password);
        user.setRole(userRole);

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
        // TODO add bcrypt hashing after sending in clear via email the password.
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
    public List<UserEntity> generateCredentials(int storeId, int userId) throws BadStoreException {
        List<UserEntity> users = null;

        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadStoreException("Cannot load store.");
        }

        // TODO call addUser().

        return users;
    }

}
