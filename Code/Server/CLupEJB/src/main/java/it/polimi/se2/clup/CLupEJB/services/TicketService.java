package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.List;

@Stateless
public class TicketService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    public List<TicketEntity> findStoreTickets(int storeId) {
        List<TicketEntity> tickets = null;

        try {
            tickets = em.createNamedQuery("TicketEntity.findByStore", TicketEntity.class)
                    .setParameter("storeId", storeId)
                    .getResultList();
        } catch (PersistenceException e) {
            System.err.println("Cannot load tickets");
        }
        return tickets;
    }

    public int getCustomersQueue(int storeId) {
        int customersQueue;
        try {
            customersQueue = Math.toIntExact(em.createNamedQuery("TicketEntity.getCustomersQueue", Long.class)
                    .setParameter(1, storeId)
                    .getSingleResult());
        } catch (ArithmeticException | PersistenceException e) {
            System.err.println("Cannot count customers in queue");
            return 0;
        }
        return customersQueue;
    }
}
