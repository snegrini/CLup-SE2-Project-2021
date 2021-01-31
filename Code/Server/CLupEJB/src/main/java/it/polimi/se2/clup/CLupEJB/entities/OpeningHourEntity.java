package it.polimi.se2.clup.CLupEJB.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Time;
import java.util.Objects;

@Entity
@Table(name = "opening_hour")
@NamedQueries({
        @NamedQuery(
                name = "OpeningHourEntity.findByStoreId",
                query = "SELECT oh FROM OpeningHourEntity oh WHERE oh.store.storeId = :storeId ORDER BY oh.weekDay"
        ),
        @NamedQuery(
                name = "OpeningHourEntity.findByStoreIdAndWeekDay",
                query = "SELECT oh FROM OpeningHourEntity oh WHERE oh.store.storeId = :storeId AND oh.weekDay = :weekDay"
        ),
})
public class OpeningHourEntity {

    @Id
    @Column(name = "opening_hours_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private int openingHoursId;

    @Column(name = "from_time")
    private Time fromTime;

    @Column(name = "to_time")
    private Time toTime;

    @Column(name = "week_day")
    private int weekDay;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonBackReference
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpeningHourEntity that = (OpeningHourEntity) o;
        return weekDay == that.weekDay && fromTime.equals(that.fromTime) && toTime.equals(that.toTime) && store.equals(that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromTime, toTime, weekDay, store);
    }
}
