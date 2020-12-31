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
    private AddressEntity addressEntity;

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

    @OneToMany(mappedBy = "storeEntity")
    private List<OpeningHourEntity> openingHourEntities;

    @OneToMany(mappedBy = "storeEntity")
    private List<TicketEntity> ticketEntities;

    @OneToMany(mappedBy = "storeEntity")
    private List<UserEntity> userEntities;


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

    public AddressEntity getAddressEntity() {
        return addressEntity;
    }

    public void setAddressEntity(AddressEntity address) {
        this.addressEntity = address;
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

    public List<OpeningHourEntity> getOpeningHourEntities() {
        return openingHourEntities;
    }

    public void setOpeningHourEntities(List<OpeningHourEntity> openingHourEntities) {
        this.openingHourEntities = openingHourEntities;
    }

    public List<TicketEntity> getTicketEntities() {
        return ticketEntities;
    }

    public void setTicketEntities(List<TicketEntity> ticketEntities) {
        this.ticketEntities = ticketEntities;
    }

    public List<UserEntity> getUserEntities() {
        return userEntities;
    }

    public void setUserEntities(List<UserEntity> userEntities) {
        this.userEntities = userEntities;
    }
}
