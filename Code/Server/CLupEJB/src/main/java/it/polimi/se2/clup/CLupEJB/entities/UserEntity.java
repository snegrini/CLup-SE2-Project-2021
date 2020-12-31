package it.polimi.se2.clup.CLupEJB.entities;

import it.polimi.se2.clup.CLupEJB.enums.UserRole;

import javax.persistence.*;

@Entity
@Table(name = "user", schema = "np_clup")
@NamedQueries({
    @NamedQuery(name = "UserEntity.checkCredentials", query = "SELECT u FROM UserEntity u WHERE u.usercode = ?1 and u.password = ?2"),
})
public class UserEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(name = "user_code")
    private String usercode;

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

    public String getUsercode() {
        return usercode;
    }

    public void setUsercode(String usercode) {
        this.usercode = usercode;
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
