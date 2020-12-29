package it.polimi.se2.clup.CLupEJB.entities;

import it.polimi.se2.clup.CLupEJB.enums.UserRole;

import javax.persistence.*;

@Entity
@Table(name = "user", schema = "np_clup")
public class UserEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private StoreEntity storeEntity;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public StoreEntity getStoreEntity() {
        return storeEntity;
    }

    public void setStoreEntity(StoreEntity storeEntity) {
        this.storeEntity = storeEntity;
    }
}
