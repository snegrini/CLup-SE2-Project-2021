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

    @Column(name = "from")
    private Time from;

    @Column(name = "to")
    private Time to;

    @Column(name = "week_day")
    private int weekDay;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private StoreEntity storeEntity;

    public int getOpeningHoursId() {
        return openingHoursId;
    }

    public void setOpeningHoursId(int openingHoursId) {
        this.openingHoursId = openingHoursId;
    }

    public Time getFrom() {
        return from;
    }

    public void setFrom(Time from) {
        this.from = from;
    }

    public Time getTo() {
        return to;
    }

    public void setTo(Time to) {
        this.to = to;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public StoreEntity getStoreEntity() {
        return storeEntity;
    }

    public void setStoreEntity(StoreEntity storeEntity) {
        this.storeEntity = storeEntity;
    }
}
