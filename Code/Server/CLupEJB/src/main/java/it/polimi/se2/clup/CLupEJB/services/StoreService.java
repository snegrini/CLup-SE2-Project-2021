package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.AddressEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.sql.Date;
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

    public StoreService(EntityManager em, OpeningHourService ohService, UserService userService) {
        this.em = em;
        this.ohService = ohService;
        this.userService = userService;
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
        List<StoreEntity> stores;

        try {
            stores = em.createNamedQuery("StoreEntity.findAll", StoreEntity.class)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadStoreException("Could not load stores");
        }
        return stores;
    }

    /**
     * Finds all the stores that starts with the filter string.
     *
     * @param filter string prompted.
     * @return the list of stores retrieved.
     * @throws BadStoreException when occurs an issue with the persistence.
     */
    public List<StoreEntity> findAllStoresFiltered(String filter) throws BadStoreException {
        List<StoreEntity> stores;
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
     * @param storeName     the name of the store.
     * @param pec           the PEC email address of the store.
     * @param phone         the phone number of the store.
     * @param addressEntity the address entity of the store.
     * @param ohFromMap     the map of the FROM opening hours.
     * @param ohToMap       the map of the TO opening hours.
     * @param userId        the ID of the user who is performing the action.
     * @return the generated users entity of the new store.
     */
    public List<Map.Entry<String, String>> addStore(String storeName, String pec, String phone, String imagePath, AddressEntity addressEntity, Map<Integer, List<Time>> ohFromMap, Map<Integer, List<Time>> ohToMap, int userId) throws BadStoreException, UnauthorizedException {
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
            ohService.addAllOpeningHour(store, ohFromMap, ohToMap, userId);
        } catch (BadOpeningHourException e) {
            em.remove(store);
            throw new BadStoreException("Store has not been added. " + e.getMessage());
        }

        List<Map.Entry<String, String>> genUsers;
        // Generate manager and employee credentials.
        try {
            genUsers = userService.generateCredentials(store, userId);
        } catch (BadStoreException | UnauthorizedException e) {
            em.remove(store);
            throw new BadStoreException("Failed to generate credentials, store has not been added.");
        }
        return genUsers;
    }

    /**
     * Updates the store cap of a store.
     *
     * @param storeCap the desired store cap.
     * @param storeId the ID of the store.
     * @param userId the ID of the user who perform the action.
     * @throws BadStoreException when occurs an issue with the persistence
     * @throws UnauthorizedException when a user without permission tries to perform the action
     */
    public void updateStoreCap(int storeCap, int storeId, int userId) throws BadStoreException, UnauthorizedException {
        StoreEntity store = em.find(StoreEntity.class, storeId);
        UserEntity user = em.find(UserEntity.class, userId);

        if (store == null || user == null) {
            throw new BadStoreException("Cannot load store or user.");
        }
        if (user.getRole() != UserRole.MANAGER || store.getStoreId() != user.getStore().getStoreId()) {
            throw new UnauthorizedException("Unauthorized operation.");
        }
        if (storeCap < store.getStoreCap() && store.getCustomersInside() >= store.getStoreCap()) {
            throw new BadStoreException("Cannot update the store cap, too many customers inside.");
        }

        store.setStoreCap(storeCap);
        em.merge(store);
    }

    /**
     * Gives the amount time to wait to enter the given store. Returns an integer value representing minutes.
     *
     * @param storeId the id of the store.
     * @return {@code 0} if there is no wait time (the store is not full), {@code 15} if the store is full but queue is empty,
     * then it will return the last ticket arrival time plus 15 minutes.
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

        Time lastTime = findLastTicketTime(store);

        if (lastTime == null) {
            return 0;
        }

        long timestamp = new java.util.Date().getTime();
        long timeDiff = lastTime.getTime() - timestamp;

        if (timeDiff < 0) {
            return 15;
        }

        return Math.toIntExact(timeDiff) / 60000 + 15;
    }

    private Time findLastTicketTime(StoreEntity store) {
        long timestamp = new java.util.Date().getTime();
        Date today = Date.valueOf(new Date(timestamp).toString());

        TicketEntity lastTicket = em.createNamedQuery("TicketEntity.findByStoreSorted", TicketEntity.class)
                .setParameter("storeId", store.getStoreId())
                .setParameter("date", today)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (lastTicket == null) {
            return null;
        }
        return lastTicket.getArrivalTime();
    }
}
