package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import org.eclipse.persistence.mappings.foundation.MapKeyMapping;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class OpeningHourService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    private final static int MAX_OPENING_HOURS = 2;

    public OpeningHourEntity findOpeningHourById(int ohId) {
        return em.find(OpeningHourEntity.class, ohId);
    }

    private void addOpeningHour(int weekDay, Time fromTime, Time toTime, int storeId) throws BadOpeningHourException {

        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadOpeningHourException("Cannot load store.");
        }

        long numOhStore = store.getOpeningHours().stream()
                .filter(ohFilter -> ohFilter.getWeekDay() == weekDay)
                .filter(ohFilter -> ohFilter.getFromTime() == fromTime || ohFilter.getToTime() == toTime)
                .count();

        if (numOhStore > 0L) {
            throw new BadOpeningHourException("Opening hour already defined for that day.");
        }

        OpeningHourEntity oh = new OpeningHourEntity();
        oh.setFromTime(fromTime);
        oh.setToTime(toTime);
        oh.setWeekDay(weekDay);
        oh.setStore(store);

        store.addOpeningHour(oh);

        em.persist(store);
    }

    private void addAllOpeningHour(List<OpeningHourEntity> ohList) throws BadOpeningHourException {
        for (OpeningHourEntity oh : ohList) {
            addOpeningHour(oh.getWeekDay(), oh.getFromTime(), oh.getToTime(), oh.getStore().getStoreId());
        }
    }

    /**
     * Adds the opening hours for a specific day.
     *
     * @param weekDay day of the week for which update the opening hours.
     * @param fromTimeList the list of times to start an opening hour.
     * @param toTimeList the list of times to end an opening hour.
     * @param store the store entity to be updated.
     * @throws BadOpeningHourException when the list is null, empty or week day are not the same
     *                                 for all the elements in the list.
     */
    public void addAllOpeningHour(int weekDay, List<Time> fromTimeList, List<Time> toTimeList, StoreEntity store)
            throws BadOpeningHourException {

        List<OpeningHourEntity> ohList = buildOpeningHourList(weekDay, fromTimeList, toTimeList, store);

        if (hasOverlap(ohList)) {
            throw new BadOpeningHourException("Two opening hours are overlapping.");
        }

        addAllOpeningHour(ohList);
    }

    public void addAllOpeningHour(StoreEntity store, Map<Integer, List<Time>> ohFromMap, Map<Integer, List<Time>> ohToMap)
            throws BadOpeningHourException {

        if (store == null) {
            throw new BadOpeningHourException("Bad store parameter.");
        }

        for (Integer day : ohFromMap.keySet()) {
            List<OpeningHourEntity> ohList = buildOpeningHourList(day, ohFromMap.get(day), ohToMap.get(day), store);

            if (hasOverlap(ohList)) {
                throw new BadOpeningHourException("Two opening hours are overlapping.");
            }

            if (!ohToMap.containsKey(day)) {
                throw new BadOpeningHourException("Opening hours missing from-to fields.");
            }
            addAllOpeningHour(day, ohFromMap.get(day), ohToMap.get(day), store);
        }
    }

    public void deleteOpeningHour(int ohId, int userId) throws BadOpeningHourException {
        UserEntity user = em.find(UserEntity.class, userId);
        OpeningHourEntity oh = em.find(OpeningHourEntity.class, ohId);

        if (user == null || oh == null) {
            throw new BadOpeningHourException("User or opening hour not found.");
        }

        // Check if user is trying to delete an opening hour of another store.
        if (oh.getStore().getStoreId() != user.getStore().getStoreId()) {
            throw new BadOpeningHourException("User not authorized to delete this opening hour.");
        }

        StoreEntity store = user.getStore();
        store.removeOpeningHour(oh); // Updates both directions of the relationship.
        em.remove(oh);
    }

    public void deleteAllOpeningHour(List<OpeningHourEntity> ohList, int userId) throws BadOpeningHourException {
        for (OpeningHourEntity oh : ohList) {
            deleteOpeningHour(oh.getOpeningHoursId(), userId);
        }
    }

    /**
     * Updates the opening hours for a specific day.
     * All the opening hour in the list must refer to the same store id and week day.
     *
     * @param weekDay day of the week for which update the opening hours.
     * @param fromTimeList the list of times to start an opening hour.
     * @param toTimeList the list of times to end an opening hour.
     * @param store the store entity to be updated.
     * @param userId the user id who is performing the update.
     * @throws BadOpeningHourException when the list is null, empty or week day are not the same
     *                                 for all the elements in the list.
     */
    public void updateOpeningHour(int weekDay, List<Time> fromTimeList, List<Time> toTimeList, StoreEntity store, int userId) throws BadOpeningHourException {
        List<OpeningHourEntity> ohList = buildOpeningHourList(weekDay, fromTimeList, toTimeList, store);

        /*if (ohList.size() < MAX_OPENING_HOURS) {
            throw new BadOpeningHourException("At least one opening hour is missing.");
        }*/

        if (hasOverlap(ohList)) {
            throw new BadOpeningHourException("Two opening hours are overlapping.");
        }

        List<OpeningHourEntity> ohStoredList = em.createNamedQuery("OpeningHourEntity.findByStoreIdAndWeekDay", OpeningHourEntity.class)
                .setParameter("storeId", store.getStoreId())
                .setParameter("weekDay", weekDay)
                .getResultList();

        // Preparing for update by deleting previous values of opening hours.
        if (!ohStoredList.isEmpty()) {
            deleteAllOpeningHour(ohStoredList, userId);
        }
        addAllOpeningHour(ohList);
    }

    public void updateAllOpeningHour(int storeId, Map<Integer, List<Time>> ohFromMap, Map<Integer, List<Time>> ohToMap, int userId) throws BadOpeningHourException {
        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadOpeningHourException("Cannot load store.");
        }

        for (Integer day : ohFromMap.keySet()) {
            if (!ohToMap.containsKey(day)) {
                throw new BadOpeningHourException("Opening hours missing from-to fields.");
            }
            updateOpeningHour(day, ohFromMap.get(day), ohToMap.get(day), store, userId);
        }
    }

    /**
     * Checks for any overlapping in the opening hours provided. Note that comparison is not strict.
     *
     * @param ohList the list of opening hour to be checked.
     * @return {@code true} if overlaps are found, {@code false} otherwise.
     */
    public boolean hasOverlap(List<OpeningHourEntity> ohList) {
        for (int i = 0; i < ohList.size() - 1; i++) {
            OpeningHourEntity oh1 = ohList.get(i);
            OpeningHourEntity oh2 = ohList.get(i + 1);

            if (oh1.getFromTime().before(oh2.getToTime()) && oh2.getFromTime().before(oh1.getToTime())) {
                return true;
            }
        }
        return false;
    }

    private List<OpeningHourEntity> buildOpeningHourList(int weekDay, List<Time> fromTimeList, List<Time> toTimeList, StoreEntity store)
        throws BadOpeningHourException {
        if (fromTimeList.size() != toTimeList.size()) {
            throw new BadOpeningHourException("Opening hours are not fully specified.");
        }

        List<OpeningHourEntity> ohList = new ArrayList<>();

        for (int i = 0; i < fromTimeList.size(); i++) {
            OpeningHourEntity oh = new OpeningHourEntity();
            oh.setWeekDay(weekDay);
            oh.setFromTime(fromTimeList.get(i));
            oh.setToTime(toTimeList.get(i));
            oh.setStore(store);
            ohList.add(oh);
        }
        return ohList;
    }

}