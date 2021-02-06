package it.polimi.se2.clup.CLupEJB.entities;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store")
@NamedQueries({
        @NamedQuery(name = "StoreEntity.findAll", query = "SELECT s FROM StoreEntity s"),
        @NamedQuery(name = "StoreEntity.findAllFiltered", query = "SELECT s FROM StoreEntity s WHERE s.storeName LIKE :filter"),
        @NamedQuery(name = "StoreEntity.findByName", query = "SELECT s FROM StoreEntity s WHERE s.storeName = :storeName"),
        @NamedQuery(name = "StoreEntity.findByPec", query = "SELECT s FROM StoreEntity s WHERE s.pecEmail = :pecEmail"),
})

public class StoreEntity {

    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int storeId;

    @Column(name = "store_name")
    private String storeName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private AddressEntity address;

    @Column(name = "pec_email")
    private String pecEmail;

    @Column(name = "phone")
    private String phone;

    @Column(name = "store_cap")
    @JsonIgnore
    private int storeCap;

    @Column(name = "customers_inside")
    @JsonIgnore
    private int customersInside;

    @Column(name = "default_pass_code")
    @JsonIgnore
    private String defaultPassCode;

    @Column(name = "image_path")
    private String imagePath;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @OrderBy("weekDay")
    @JsonManagedReference
    private final List<OpeningHourEntity> openingHours = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @JsonBackReference
    private final List<TicketEntity> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @JsonBackReference
    private final List<UserEntity> users = new ArrayList<>();

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public String getPecEmail() {
        return pecEmail;
    }

    public void setPecEmail(String pecEmail) {
        this.pecEmail = pecEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getStoreCap() {
        return storeCap;
    }

    public void setStoreCap(int storeCap) {
        this.storeCap = storeCap;
    }

    public int getCustomersInside() {
        return customersInside;
    }

    public void setCustomersInside(int customersInside) {
        this.customersInside = customersInside;
    }

    public String getDefaultPassCode() {
        return defaultPassCode;
    }

    public void setDefaultPassCode(String defaultPassCode) {
        this.defaultPassCode = defaultPassCode;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<OpeningHourEntity> getOpeningHours() {
        return openingHours;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    /**
     * Adds an opening hour to the store.
     * Each side of the relationship is updated.
     * If opening hour is new, invoking persist() on store cascades also to opening hour.
     *
     * @param oh The opening hour to be added.
     */
    public void addOpeningHour(OpeningHourEntity oh) {
        getOpeningHours().add(oh);
        oh.setStore(this);
    }

    /**
     * Removes an opening hour to the store.
     * Each side of the relationship is updated.
     *
     * @param oh The opening hour to be removed.
     */
    public void removeOpeningHour(OpeningHourEntity oh) {
        getOpeningHours().remove(oh);
    }

    /**
     * Adds a user to the store.
     * Each side of the relationship is updated.
     *
     * @param user The user to be added.
     */
    public void addUser(UserEntity user) {
        getUsers().add(user);
        user.setStore(this);
    }

    /**
     * Removes a user to the store.
     * Each side of the relationship is updated.
     *
     * @param user The user to be added.
     */
    public void removeUser(UserEntity user) {
        getUsers().remove(user);
    }

    /**
     * Adds a ticket to the store.
     * Each side of the relationship is updated.
     *
     * @param ticket The ticket to be added.
     */
    public void addTicket(TicketEntity ticket) {
        getTickets().add(ticket);
        ticket.setStore(this);
    }

    /**
     * Removes a ticket to the store.
     * Each side of the relationship is updated.
     *
     * @param ticket The ticket to be removed.
     */
    public void removeTicket(TicketEntity ticket) {
        getTickets().remove(ticket);
    }
}
