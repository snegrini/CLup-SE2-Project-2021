package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.*;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;

import javax.ejb.DuplicateKeyException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
public class StoreService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/OpeningHourService")
    private OpeningHourService ohService;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/UserService")
    private UserService userService;

    public StoreService() {
    }

    public StoreService(EntityManager em) {
        this.em = em;
    }

    public StoreEntity findStoreById(int storeId) {
        return em.find(StoreEntity.class, storeId);
    }

    public StoreEntity findStoreByName(String name) {
        return em.createNamedQuery("StoreEntity.findByName", StoreEntity.class)
                .setParameter("storeName", name)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public StoreEntity findStoreByPec(String pec) {
        return em.createNamedQuery("StoreEntity.findByPec", StoreEntity.class)
                .setParameter("pecEmail", pec)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<StoreEntity> findAllStores() throws BadStoreException {
        List<StoreEntity> stores = null;

        try {
            stores = em.createNamedQuery("StoreEntity.findAll", StoreEntity.class)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadStoreException("Could not load stores");
        }
        return stores;
    }

    public List<StoreEntity> findAllStoresFiltered(String filter) throws BadStoreException {
        List<StoreEntity> stores = null;
        filter += "%";

        try {
            stores = em.createNamedQuery("StoreEntity.findAllFiltered", StoreEntity.class)
                    .setParameter("filter", filter)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadStoreException("Could not load stores");
        }
        return stores;
    }

    /**
     * Adds a new store to the system.
     *
     * @param storeName the name of the store.
     * @param pec the PEC email address of the store.
     * @param phone the phone number of the store.
     * @param addressEntity the address entity of the store.
     * @param ohFromMap
     * @param ohToMap
     * @param userId
     * @return the generated users entity of the new store.
     */
    public List<Map.Entry<String, String>> addStore(String storeName, String pec, String phone, String imagePath, AddressEntity addressEntity, Map<Integer, List<Time>> ohFromMap, Map<Integer, List<Time>> ohToMap, int userId) throws BadStoreException, UnauthorizedException {
        UserEntity user = em.find(UserEntity.class, userId);

        if (user == null) {
            throw new BadStoreException("Cannot load user.");
        }
        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Unauthorized operation.");
        }

        if (findStoreByName(storeName) != null) {
            throw new BadStoreException("A store have already registered with same name.");
        }
        if (findStoreByPec(pec) != null) {
            throw new BadStoreException("A store have already registered with same pec address.");
        }



        StoreEntity store = new StoreEntity();
        String passCode = UUID.randomUUID().toString().substring(0, 8);

        store.setStoreName(storeName);
        store.setPecEmail(pec);
        store.setPhone(phone);
        store.setImagePath(imagePath);
        store.setAddress(addressEntity);
        store.setDefaultPassCode(passCode);

        em.persist(store);

        // Add opening hours to the created store.
        try {
            ohService.addAllOpeningHour(store, ohFromMap, ohToMap);
        } catch (BadOpeningHourException e) {
            em.remove(store);
            throw new BadStoreException("Bad opening hour, store has not been added.");
        }

        List<Map.Entry<String, String>> genUsers;
        // Generate manager and employee credentials.
        try {
            genUsers = userService.generateCredentials(store.getStoreId(), userId);
        } catch (BadStoreException | UnauthorizedException e) {
            em.remove(store);
            throw new BadStoreException("Failed to generate credentials, store has not been added.");
        }
        return genUsers;
    }

    public void updateStoreCap(int storeCap, int storeId, int userId) throws BadStoreException, UnauthorizedException {
        StoreEntity store = em.find(StoreEntity.class, storeId);
        UserEntity user = em.find(UserEntity.class, userId);

        if (store == null || user == null) {
            throw new BadStoreException("Cannot load store or user.");
        }
        if (user.getRole() != UserRole.MANAGER || store.getStoreId() != user.getStore().getStoreId()) {
            throw new UnauthorizedException("Unauthorized operation.");
        }

        store.setStoreCap(storeCap);
        em.merge(store);
    }

    /**
     * Gives the amount time to wait to enter the given store. Returns an integer value representing minutes.
     *
     * @param storeId the id of the store.
     * @return {@code 0} if there is no wait time (the store is not full), {@code 15} otherwise.
     * @throws BadStoreException if no store can be found.
     */
    public int getEstimateTime(int storeId) throws BadStoreException {
        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadStoreException("Cannot load store.");
        }

        if (store.getCustomersInside() < store.getStoreCap()) {
            return 0;
        }
        return 15;
    }
}
