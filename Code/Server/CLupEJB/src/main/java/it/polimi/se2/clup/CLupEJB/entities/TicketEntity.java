package it.polimi.se2.clup.CLupEJB.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.polimi.se2.clup.CLupEJB.enums.PassStatus;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name = "ticket", schema = "np_clup")
@NamedQueries({
        @NamedQuery(name = "TicketEntity.findByStore", query = "SELECT t FROM TicketEntity t WHERE t.store.storeId = :storeId"),
        @NamedQuery(name = "TicketEntity.findByStoreSorted", query = "SELECT t FROM TicketEntity t WHERE t.store.storeId = :storeId ORDER BY t.issuedAt DESC"),
        @NamedQuery(name = "TicketEntity.findByCustomerId", query = "SELECT t FROM TicketEntity t WHERE t.customerId = :customerId"),
        @NamedQuery(name = "TicketEntity.findByPassCode", query = "SELECT t FROM TicketEntity t WHERE t.passCode = :passCode"),
})
@NamedNativeQueries({
    @NamedNativeQuery(
            name = "TicketEntity.getCustomersQueue",
            query = "SELECT COUNT(*) FROM np_clup.ticket " +
                    "WHERE store_id = ? " +
                    "AND PASS_STATUS = 'VALID' " +
                    "AND date = DATE(NOW())" +
                    "AND TIMEDIFF(arrival_time, TIME(NOW())) BETWEEN TIME('-00:15:00') AND TIME('00:15:00')"
    )
})
public class TicketEntity {
    @Id
    @Column(name = "ticket_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ticketId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "pass_code")
    private String passCode;

    @Column(name = "pass_status")
    @Enumerated(EnumType.STRING)
    private PassStatus passStatus;

    @Column(name = "queue_number")
    private int queueNumber;

    @Column(name = "date")
    private Date date;

    @Column(name = "arrival_time")
    private Time arrivalTime;

    @Column(name = "issued_at")
    private Timestamp issuedAt;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonManagedReference
    private StoreEntity store;

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

    public PassStatus getPassStatus() {
        return passStatus;
    }

    public void setPassStatus(PassStatus passStatus) {
        this.passStatus = passStatus;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Timestamp getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Timestamp issuedAt) {
        this.issuedAt = issuedAt;
    }

    public StoreEntity getStore() {
        return store;
    }

    public void setStore(StoreEntity store) {
        this.store = store;
    }
}
