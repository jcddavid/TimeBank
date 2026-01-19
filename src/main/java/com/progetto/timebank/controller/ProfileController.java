package com.progetto.timebank.controller;

import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.UserDAO;
import java.sql.SQLException;

public class ProfileController {

    public double getUserRating(int userId) {
        UserDAO dao = DAOFactory.getUserDAO();
        try {
            return dao.getAverageRating(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}