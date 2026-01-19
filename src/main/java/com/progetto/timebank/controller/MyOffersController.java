package com.progetto.timebank.controller;

import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.UserDAO;
import com.progetto.timebank.exception.PersistenceException;
import java.sql.SQLException;

public class MyOffersController {

    public void updateSkills(int userId, String newSkills) throws PersistenceException {
        UserDAO dao = DAOFactory.getUserDAO();
        try {
            dao.updateUserSkills(userId, newSkills);
        } catch (SQLException e) {
            throw new PersistenceException("Errore durante l'aggiornamento delle competenze.", e);
        }
    }
}