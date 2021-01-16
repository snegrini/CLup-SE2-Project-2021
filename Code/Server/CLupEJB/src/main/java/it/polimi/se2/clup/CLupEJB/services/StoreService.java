package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.sql.Time;
import java.util.List;
import java.util.Map;

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

    public void addStore(int storeId, Map<Integer, List<Time>> ohFromMap, Map<Integer, List<Time>> ohToMap, int userId) throws BadStoreException{
    }
}
