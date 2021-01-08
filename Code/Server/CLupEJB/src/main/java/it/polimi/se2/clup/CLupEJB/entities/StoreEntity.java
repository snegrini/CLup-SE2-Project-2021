package it.polimi.se2.clup.CLupEJB.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "store", schema = "np_clup")
@NamedQueries({
    @NamedQuery(name = "StoreEntity.findAll", query = "SELECT s FROM StoreEntity s"),
})
public class StoreEntity {

    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int storeId;

    @Column(name = "store_name")
    private String storeName;

    @OneToOne
    @JoinColumn(name = "address_id")
    private AddressEntity address;

    @Column(name = "pec_email")
    private String pecEmail;

    @Column(name = "phone")
    private String phone;

    @Column(name = "store_cap")
    private int storeCap;

    @Column(name = "customers_inside")
    private int customersInside;

    @Column(name = "default_pass_code")
    private String defaultPassCode;

    @OneToMany(mappedBy = "store")
    private List<OpeningHourEntity> openingHours;

    @OneToMany(mappedBy = "store")
    private List<TicketEntity> tickets;

    @OneToMany(mappedBy = "store")
    private List<UserEntity> users;

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

    public List<OpeningHourEntity> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<OpeningHourEntity> openingHours) {
        this.openingHours = openingHours;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketEntity> tickets) {
        this.tickets = tickets;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    /**
     * Adds an opening hour to the store.
     * Each side of the relationship is updated.
     *
     * @param oh The opening hour to be added.
     */
    public void addOpeningHour(OpeningHourEntity oh) {
        getOpeningHours().add(oh);
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
}
