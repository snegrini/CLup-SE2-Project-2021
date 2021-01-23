package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.AddressEntity;
import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
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

    public StoreEntity findStoreById(int storeId) {
        return em.find(StoreEntity.class, storeId);
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
     * @return the created store entity.
     */
    public StoreEntity addStore(String storeName, String pec, String phone, String imagePath, AddressEntity addressEntity) {
        StoreEntity store = new StoreEntity();
        String passCode = UUID.randomUUID().toString().substring(0, 8);

        store.setStoreName(storeName);
        store.setPecEmail(pec);
        store.setPhone(phone);
        store.setImagePath(imagePath);
        store.setAddress(addressEntity);
        store.setDefaultPassCode(passCode);

        em.persist(store);
        return store;
    }

}
