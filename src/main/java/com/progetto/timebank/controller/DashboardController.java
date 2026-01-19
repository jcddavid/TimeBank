package com.progetto.timebank.controller;

import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.UserDAO;
import com.progetto.timebank.model.User;
import java.sql.SQLException;

public class DashboardController {

    public void refreshSessionData() {
        try {
            UserSession session = UserSession.getInstance();
            if (session.getUser() == null) return;

            int myId = session.getUser().getId();
            UserDAO dao = DAOFactory.getUserDAO();
            User freshUser = dao.getUserById(myId);

            if (freshUser != null) {
                session.getUser().setBalance(freshUser.getBalance());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}