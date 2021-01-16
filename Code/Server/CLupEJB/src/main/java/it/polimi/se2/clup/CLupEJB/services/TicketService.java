package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Stateless
public class TicketService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    public List<TicketEntity> findStoreTickets(int storeId) throws BadTicketException {
        List<TicketEntity> tickets = null;

        try {
            tickets = em.createNamedQuery("TicketEntity.findByStore", TicketEntity.class)
                    .setParameter("storeId", storeId)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadTicketException("Cannot load tickets");
        }
        return tickets;
    }

    public int getCustomersQueue(int storeId) throws BadTicketException {
        int customersQueue;
        try {
            customersQueue = Math.toIntExact(em.createNamedQuery("TicketEntity.getCustomersQueue", Long.class)
                    .setParameter(1, storeId)
                    .getSingleResult());
        } catch (ArithmeticException | PersistenceException e) {
            throw new BadTicketException("Cannot count customers in queue");
        }
        return customersQueue;
    }

    public List<TicketEntity> findCustomerTickets(String customerId) throws BadTicketException {
        List<TicketEntity> tickets = null;

        try {
            tickets = em.createNamedQuery("TicketEntity.findByCustomerId", TicketEntity.class)
                    .setParameter("customerId", customerId)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadTicketException("Cannot load tickets");
        }
        return tickets;
    }

    public void updateTicketStatus(String passCode, int storeId) throws BadTicketException {
        TicketEntity ticket = em.createNamedQuery("TicketEntity.findByPassCode", TicketEntity.class)
                .setParameter("passCode", passCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        // TODO Check store default code before throwing exception
        if (ticket == null) {
            throw new BadTicketException("Invalid pass code");
        }

        if (ticket.getStore().getStoreId() != storeId) {
            throw new BadTicketException("Unauthorized operation");
        }

        switch (ticket.getPassStatus()) {
            case VALID:
                ticket.setPassStatus(PassStatus.USED);
                break;
            case USED:
                ticket.setPassStatus(PassStatus.EXPIRED);
                break;
            case EXPIRED:
                throw new BadTicketException("Ticket already expired");
        }

        em.merge(ticket);
    }

    public TicketEntity addTicket(String customerId, int storeId) throws BadTicketException {
        TicketEntity ticket = new TicketEntity();
        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadTicketException("Cannot load store");
        }

        String passCode;
        TicketEntity collisionTicket;
        do {
            // Creating pass code
            passCode = UUID.randomUUID().toString().substring(0, 8);

            // Checking pass code uniqueness
            collisionTicket = em.createNamedQuery("TicketEntity.findByPassCode", TicketEntity.class)
                    .setParameter("passCode", passCode)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } while (collisionTicket != null);

        // Fetching last emitted ticket
        TicketEntity lastTicket;
        try {
            lastTicket = em.createNamedQuery("TicketEntity.findByStoreSorted", TicketEntity.class)
                    .setParameter("storeId", storeId)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (lastTicket == null) {
                throw new BadTicketException("Cannot load tickets");
            }
        } catch (PersistenceException e) {
            throw new BadTicketException("Cannot load tickets");
        }

        Time ticketTime = new Time(lastTicket.getArrivalTime().getTime() + 900000); // Last ticket time + 15 min

        ticket.setCustomerId(customerId);
        ticket.setPassCode(passCode);
        ticket.setPassStatus(PassStatus.VALID);
        ticket.setQueueNumber(lastTicket.getQueueNumber() + 1);
        ticket.setArrivalTime(ticketTime);
        ticket.setStore(store);
        ticket.setDate(new Date(System.currentTimeMillis()));
        ticket.setIssuedAt(new Timestamp(System.currentTimeMillis()));

        // TODO Check if already a ticket for that day or if exceed the opening hours

        // FIXME probably shall change persist to Store.
        em.persist(ticket);
        return ticket;
    }

    public void deleteTicket(String customerId, String passCode) throws BadTicketException {
        TicketEntity ticket = em.createNamedQuery("TicketEntity.findByPassCode", TicketEntity.class)
                .setParameter("passCode", passCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (ticket == null) {
            throw new BadTicketException("Invalid pass code");
        }

        if (!ticket.getCustomerId().equals(customerId)) {
            throw new BadTicketException("Unauthorized operation");
        }

        em.remove(ticket);
    }
}
