package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.List;

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
            stores = em.createNamedQuery("StoreEntity.findAll", StoreEntity.class).getResultList();
        } catch (PersistenceException e) {
            throw new BadStoreException("Could not load stores");
        }
        return stores;
    }
}
