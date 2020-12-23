package it.polimi.se2.clup.CLupEJB.services;

import it.polimi.se2.clup.CLupEJB.entities.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class UserService {
    @PersistenceContext(unitName = "CLupEJB")
    private EntityManager em;

    public UserService() {
    }

    public User checkCredentials(String username, String password) {
    }
}
