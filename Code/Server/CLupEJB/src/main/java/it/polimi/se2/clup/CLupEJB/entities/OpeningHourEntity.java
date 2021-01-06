package it.polimi.se2.clup.CLupEJB.entities;

import javax.persistence.*;
import java.sql.Time;

@Entity
@Table(name = "opening_hour", schema = "np_clup")
public class OpeningHourEntity {

    @Id
    @Column(name = "opening_hours_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int openingHoursId;

    @Column(name = "from_time")
    private Time fromTime;

    @Column(name = "to_time")
    private Time toTime;

    @Column(name = "week_day")
    private int weekDay;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    public int getOpeningHoursId() {
        return openingHoursId;
    }

    public void setOpeningHoursId(int openingHoursId) {
        this.openingHoursId = openingHoursId;
    }

    public Time getFromTime() {
        return fromTime;
    }

    public void setFromTime(Time fromTime) {
        this.fromTime = fromTime;
    }

    public Time getToTime() {
        return toTime;
    }

    public void setToTime(Time toTime) {
        this.toTime = toTime;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public StoreEntity getStore() {
        return store;
    }

    public void setStore(StoreEntity store) {
        this.store = store;
    }
}
