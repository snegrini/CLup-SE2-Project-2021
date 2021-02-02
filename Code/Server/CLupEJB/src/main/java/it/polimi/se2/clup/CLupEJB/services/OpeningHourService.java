package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.*;

@Stateless
public class OpeningHourService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    private final static int MAX_OPENING_HOURS = 2;

    public OpeningHourService() {
    }

    public OpeningHourService(EntityManager em) {
        this.em = em;
    }

    public OpeningHourEntity findOpeningHourById(int ohId) {
        return em.find(OpeningHourEntity.class, ohId);
    }

    private void addOpeningHour(int weekDay, Time fromTime, Time toTime, StoreEntity store) throws BadOpeningHourException {

        if (store == null) {
            throw new BadOpeningHourException("Bad store parameter.");
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
            addOpeningHour(oh.getWeekDay(), oh.getFromTime(), oh.getToTime(), oh.getStore());
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
        if (hasFromAfterTo(ohList)) {
            throw new BadOpeningHourException("From time cannot be after to time.");
        }
        if (isBorderline(ohList)) {
            throw new BadOpeningHourException("Opening hour must finish before 23:45.");
        }

        addAllOpeningHour(ohList);
    }

    /**
     * Checks if an opening hour has the from-time after the to-time.
     *
     * @param ohList the list of opening hour to be checked.
     * @return {@code true} if an opening hour has the from-time after the to-time, {@code false} otherwise.
     */
    private boolean hasFromAfterTo(List<OpeningHourEntity> ohList) {
        for (OpeningHourEntity oh : ohList) {
            if (oh.getFromTime().after(oh.getToTime())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an opening hour is borderline, i.e. it is defined between 23:45:00 and 00:00:00 which is a forbidden time.
     *
     * @param ohList the list of opening hour to be checked.
     * @return {@code true} if an opening hour is borderline, {@code false} otherwise.
     */
    private boolean isBorderline(List<OpeningHourEntity> ohList) {
        Time badTimeFrom = new Time(81900000); // 23:45:00

        for (OpeningHourEntity oh : ohList) {
            Time time = Time.valueOf(oh.getToTime().toString());
            if (!time.before(badTimeFrom)) {
                return true;
            }
        }
        return false;
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
        if (user.getRole().equals(UserRole.EMPLOYEE) || oh.getStore().getStoreId() != user.getStore().getStoreId()) {
            throw new BadOpeningHourException("User not authorized to delete this opening hour.");
        }

        StoreEntity store = user.getStore();
        store.removeOpeningHour(oh); // Updates both directions of the relationship.
        em.remove(oh);
    }

    /**
     * Delete all opening hours given a week day and a store.
     *
     * @param weekDay the week day of the opening hours to delete.
     * @param store the store of the opening hours.
     * @param userId the user id performing the operation.
     * @throws BadOpeningHourException when user is unauthorized or when opening hour is not found.
     */
    public void deleteOpeningHours(int weekDay, StoreEntity store, int userId) throws BadOpeningHourException {
        List<OpeningHourEntity> ohStoredList = em.createNamedQuery("OpeningHourEntity.findByStoreIdAndWeekDay", OpeningHourEntity.class)
                .setParameter("storeId", store.getStoreId())
                .setParameter("weekDay", weekDay)
                .getResultList();

        deleteAllOpeningHour(ohStoredList, userId);
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

        for (DayOfWeek day : DayOfWeek.values()) {
            Integer dayNum = day.getValue();

            // ^ is the XOR operator.
            if (ohFromMap.containsKey(dayNum) ^ ohToMap.containsKey(dayNum)) {
                throw new BadOpeningHourException("Opening hours missing from-to fields.");
            } else if (!ohFromMap.containsKey(dayNum) && !ohToMap.containsKey(dayNum)) {
                // Unchecked opening hour, performing a delete.
                deleteOpeningHours(dayNum, store, userId);
            } else {
                // Opening hour must be updated.
                updateOpeningHour(dayNum, ohFromMap.get(dayNum), ohToMap.get(dayNum), store, userId);
            }
        }
    }

    /**
     * Checks if a timestamp hits an opening hour of the same week day.
     *
     * @param storeId the store id to look for the opening hours.
     * @param time the time to check.
     * @return {@code true} if an opening hours has been found, {@code false} otherwise.
     * @throws BadOpeningHourException if no store could be found.
     */
    public boolean isInOpeningHour(int storeId, Time time) throws BadOpeningHourException {
        StoreEntity store = em.find(StoreEntity.class, storeId);

        if (store == null) {
            throw new BadOpeningHourException("Cannot load store.");
        }

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(new SimpleDateFormat("EEEE", Locale.US).format(new Date()).toUpperCase());
        int weekDay = dayOfWeek.getValue();

        List<OpeningHourEntity> ohStoredList = em.createNamedQuery("OpeningHourEntity.findByStoreIdAndWeekDay", OpeningHourEntity.class)
                .setParameter("storeId", store.getStoreId())
                .setParameter("weekDay", weekDay)
                .getResultList();

        for (OpeningHourEntity oh : ohStoredList) {
            if (time.after(oh.getFromTime()) && time.before(oh.getToTime())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for any overlapping in the opening hours provided. Note that comparison is not strict.
     *
     * @param ohList the list of opening hour to be checked.
     * @return {@code true} if overlaps are found, {@code false} otherwise.
     */
    private boolean hasOverlap(List<OpeningHourEntity> ohList) {
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