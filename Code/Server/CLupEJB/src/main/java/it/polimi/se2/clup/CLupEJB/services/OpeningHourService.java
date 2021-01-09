package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class OpeningHourService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    public OpeningHourEntity findOpeningHourById(int ohId) {
        return em.find(OpeningHourEntity.class, ohId);
    }

    public void deleteOpeningHour(int ohId, int userId) throws BadOpeningHourException {
        UserEntity user = em.find(UserEntity.class, userId);
        OpeningHourEntity oh = em.find(OpeningHourEntity.class, ohId);

        // Check if user is trying to delete an opening hour of another store.
        if (oh.getStore().getStoreId() != user.getStore().getStoreId()) {
            throw new BadOpeningHourException("User not authorized to delete this opening hour.");
        }

        StoreEntity store = user.getStore();
        store.removeOpeningHour(oh); // Updates both directions of the relationship.
        em.remove(oh);
    }

}