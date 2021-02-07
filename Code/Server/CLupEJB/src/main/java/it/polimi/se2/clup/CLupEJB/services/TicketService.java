package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
public class TicketService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/OpeningHourService")
    private OpeningHourService ohs;

    public TicketService() {
    }

    public TicketService(EntityManager em, OpeningHourService ohs) {
        this.em = em;
        this.ohs = ohs;
    }

    /**
     * Finds a ticket by his id.
     *
     * @param ticketId the id of the ticket to be found.
     * @return the found ticket if any, null otherwise.
     */
    public TicketEntity findTicketById(int ticketId) {
       return em.find(TicketEntity.class, ticketId);
    }

    /**
     * Finds a valid ticket by his id.
     *
     * @param ticketId the id of the ticket to be found.
     * @return the found ticket if any, null otherwise.
     */
    public TicketEntity findValidTicketById(int ticketId) {
        TicketEntity ticketFound = em.find(TicketEntity.class, ticketId);

        if (ticketFound == null) {
            return null;
        }

        List<TicketEntity> tempList = new ArrayList<>();
        tempList.add(ticketFound);

        // Verify and update tickets status.
        checkExpiredTickets(tempList);

        return tempList.isEmpty() ? null : tempList.get(0);
    }

    /**
     * Finds all the tickets of a specific store and returns them.
     *
     * @param storeId ID of the store.
     * @return a list of tickets.
     * @throws BadTicketException when occurs an issue with the persistence.
     */
    public List<TicketEntity> findStoreTickets(int storeId) throws BadTicketException {
        List<TicketEntity> tickets;

        try {
            tickets = em.createNamedQuery("TicketEntity.findByStore", TicketEntity.class)
                    .setParameter("storeId", storeId)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadTicketException("Cannot load tickets");
        }
        return tickets;
    }

    /**
     * Finds all the valid tickets of a specific store and returns them.
     * A check is performed and tickets are updated to ensure the returned tickets are still valid.
     *
     * @param storeId ID of the store.
     * @return a list of tickets.
     * @throws BadTicketException when occurs an issue with the persistence.
     */
    public List<TicketEntity> findValidStoreTickets(int storeId) throws BadTicketException {
        List<TicketEntity> tickets;

        try {
            tickets = em.createNamedQuery("TicketEntity.findByStoreAndPassStatusSorted", TicketEntity.class)
                    .setParameter("storeId", storeId)
                    .setParameter("passStatus", PassStatus.VALID)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadTicketException("Cannot load tickets");
        }

        // Verify and update tickets status.
        checkExpiredTickets(tickets);

        return tickets;
    }

    /**
     * For all the prompted tickets checks if the tickets inside the given list are expired. If so their status is updated.
     *
     * @param ticketList list of tickets to be checked.
     */
    private void checkExpiredTickets(List<TicketEntity> ticketList) {
        long timestamp = new java.util.Date().getTime();

        Date today = Date.valueOf(new Date(timestamp).toString());
        Time now = Time.valueOf(new Time(timestamp).toString());

        List<TicketEntity> expiredTickets = new ArrayList<>();

        for (TicketEntity t : ticketList) {
            Time academicTime = Time.valueOf(new Time(t.getArrivalTime().getTime() + 900000).toString()); // Last ticket time + 15 min

            if (t.getPassStatus().equals(PassStatus.USED) && today.after(t.getDate())) {
                t.setPassStatus(PassStatus.EXPIRED);
                em.merge(t);
                expiredTickets.add(t);
            } else if (t.getPassStatus().equals(PassStatus.VALID) && (today.after(t.getDate()) || now.after(academicTime))) {
                t.setPassStatus(PassStatus.EXPIRED);
                em.merge(t);
                expiredTickets.add(t);
            }
        }
        ticketList.removeAll(expiredTickets);
    }

    /**
     * Computes the number of customers in queue at a specific store and returns it.
     *
     * @param storeId ID of the store.
     * @return the number of customers in queue.
     * @throws BadTicketException when occurs an issue with the persistence.
     */
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

    /**
     * Finds all tickets of a specific customer and returns them.
     *
     * @param customerId ID of the customer.
     * @return a list of tickets.
     * @throws BadTicketException when occurs an issue with the persistence.
     */
    public List<TicketEntity> findCustomerTickets(String customerId) throws BadTicketException {
        List<TicketEntity> tickets;

        try {
            tickets = em.createNamedQuery("TicketEntity.findByCustomerId", TicketEntity.class)
                    .setParameter("customerId", customerId)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new BadTicketException("Cannot load tickets");
        }

        // Verify and update tickets status.
        checkExpiredTickets(tickets);

        return tickets;
    }

    /**
     * Updates the status of a ticket after checking the consistency with the store id.
     *
     * @param passCode code of the ticket.
     * @param storeId  ID of the store to be checked.
     * @return the new ticket status.
     * @throws BadTicketException    when occurs an issue with the persistence or is performed an invalid operation.
     * @throws UnauthorizedException if the user has no permission to update the specified ticket.
     * @throws BadStoreException     when the store is not found
     */
    public PassStatus updateTicketStatus(String passCode, int storeId) throws BadTicketException, UnauthorizedException, BadStoreException {
        TicketEntity ticket = em.createNamedQuery("TicketEntity.findByPassCode", TicketEntity.class)
                .setParameter("passCode", passCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        // Check for default pass code.
        if (ticket == null) {
            StoreEntity store = em.find(StoreEntity.class, storeId);

            if (store == null) {
                throw new BadStoreException("Invalid store ID");
            }

            if (store.getDefaultPassCode().equals(passCode)) {
                store.setCustomersInside(store.getCustomersInside() - 1);
                em.merge(store);
                return PassStatus.EXPIRED;
            } else {
                throw new BadTicketException("Invalid pass code");
            }
        }

        StoreEntity store = ticket.getStore();
        if (store.getStoreId() != storeId) {
            throw new UnauthorizedException("Unauthorized operation");
        }

        switch (ticket.getPassStatus()) {
            case VALID:
                ticket.setPassStatus(PassStatus.USED);
                store.setCustomersInside(store.getCustomersInside() + 1);
                break;
            case USED:
                ticket.setPassStatus(PassStatus.EXPIRED);
                store.setCustomersInside(store.getCustomersInside() - 1);
                break;
            case EXPIRED:
                throw new BadTicketException("Ticket already expired");
        }

        em.merge(store);
        em.merge(ticket);

        return ticket.getPassStatus();
    }

    /**
     * Creates a ticket for a customer for the current day of a specific store and returns it.
     *
     * @param customerId ID of the customer.
     * @param storeId    ID of the store.
     * @return the ticket just created
     * @throws BadTicketException when occurs an issue with the persistence or is performed an invalid operation.
     * @throws BadStoreException  when the store is not found.
     */
    public TicketEntity addTicket(String customerId, int storeId) throws BadTicketException, BadStoreException, BadOpeningHourException {
        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadStoreException("Cannot load store");
        }

        long timestamp = new java.util.Date().getTime();
        Date date = new Date(timestamp);

        if (gotAlreadyATicket(customerId, date)) {
            throw new BadTicketException("Already retrieved a ticket for today");
        }

        TicketEntity ticket = new TicketEntity();

        ticket.setCustomerId(customerId);
        ticket.setPassCode(getUniquePassCode());
        ticket.setPassStatus(PassStatus.VALID);
        ticket.setStore(store);
        ticket.setDate(new Date(System.currentTimeMillis()));
        ticket.setIssuedAt(new Timestamp(System.currentTimeMillis()));

        addQueueNumberAndTime(ticket, store, date);

        store.addTicket(ticket);
        em.persist(store);
        return ticket;
    }

    /**
     * @return a unique PassCode
     */
    private String getUniquePassCode() {
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

        return passCode;
    }

    /**
     * Checks if the user already got a ticket on that day.
     *
     * @param customerId the ID of the customer to be checked.
     * @param date       the date to be checked.
     * @return true if the user already took a ticket on that day, false otherwise.
     */
    private Boolean gotAlreadyATicket(String customerId, Date date) {
        TicketEntity alreadyRetrievedTicket = em.createNamedQuery("TicketEntity.findByCustomerIdOnDay", TicketEntity.class)
                .setParameter("customerId", customerId)
                .setParameter("date", date)
                .setParameter("passStatus", PassStatus.VALID)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        return alreadyRetrievedTicket != null;
    }

    /**
     * Sets the queue number and the arrival time to a ticket.
     *
     * @param ticket the ticket that has to be modified.
     * @param store  the store that owns the ticket
     * @param date   the date of the
     * @throws BadStoreException       when the store is not found.
     * @throws BadOpeningHourException when the store is closed.
     * @throws BadTicketException      when occurs an issue with the persistence.
     */
    private void addQueueNumberAndTime(TicketEntity ticket, StoreEntity store, Date date) throws BadStoreException, BadOpeningHourException, BadTicketException {
        try {
            TicketEntity lastTicket = em.createNamedQuery("TicketEntity.findByStoreSorted", TicketEntity.class)
                    .setParameter("storeId", store.getStoreId())
                    .setParameter("date", date)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (store.getCustomersInside() < store.getStoreCap()) {
                ticket.setArrivalTime(Time.valueOf(new Time(date.getTime()).toString()));

                if (lastTicket == null) {
                    ticket.setQueueNumber(1);
                } else {
                    ticket.setQueueNumber(lastTicket.getQueueNumber() + 1);
                }
            } else {
                if (lastTicket == null) {
                    throw new BadStoreException("Invalid store cap");
                } else {
                    ticket.setArrivalTime(new Time(lastTicket.getArrivalTime().getTime() + 900000)); // Last ticket time + 15 min
                    ticket.setQueueNumber(lastTicket.getQueueNumber() + 1);
                }
            }

            if (!ohs.isInOpeningHour(store.getStoreId(), ticket.getArrivalTime())) {
                throw new BadOpeningHourException("The store is closed");
            }
        } catch (PersistenceException e) {
            throw new BadTicketException("Cannot load tickets");
        }
    }

    /**
     * Delete a ticket from the manager side.
     *
     * @param ticketId the id of the ticket to be deleted.
     * @param userId   the id of the user who is performing the operation.
     * @throws BadTicketException    if no ticket could be found.
     * @throws UnauthorizedException if the user has no permission to delete the specified ticket.
     */
    public void deleteTicket(int ticketId, int userId) throws BadTicketException, UnauthorizedException {
        TicketEntity ticket = em.find(TicketEntity.class, ticketId);
        UserEntity user = em.find(UserEntity.class, userId);

        if (user == null) {
            throw new UnauthorizedException("Unauthorized operation.");
        }

        StoreEntity store = user.getStore();

        if (ticket == null) {
            throw new BadTicketException("Unable to find ticket.");
        }

        if (user.getRole() != UserRole.MANAGER || ticket.getStore().getStoreId() != store.getStoreId()) {
            throw new UnauthorizedException("Unauthorized operation.");
        }

        store.removeTicket(ticket);
        em.remove(ticket);
    }

    /**
     * Delete a ticket from the customer side.
     *
     * @param customerId the unique id of the customer who is performing the delete.
     * @param ticketId   the id of the ticket to be deleted.
     * @throws BadTicketException    when the passCode is invalid.
     * @throws UnauthorizedException if the user has no permission to delete the specified ticket.
     */
    public void deleteTicket(String customerId, int ticketId) throws BadTicketException, UnauthorizedException {
        TicketEntity ticket = em.find(TicketEntity.class, ticketId);

        if (ticket == null) {
            throw new BadTicketException("Invalid ticket ID");
        }

        if (!ticket.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("Unauthorized operation");
        }

        ticket.getStore().removeTicket(ticket);
        em.remove(ticket);
    }
}
