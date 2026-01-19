package com.progetto.timebank.controller;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.UserDAO;
import com.progetto.timebank.model.User;
import java.sql.SQLException;

public class LoginController {
    private final UserDAO userDAO = DAOFactory.getUserDAO();

    public boolean login(String username, String password) throws SQLException {
        User user = userDAO.getUserByUsernameAndPassword(username, password);
        if (user != null) {
            UserBean bean = UserBean.fromUser(user);
            UserSession.getInstance().setUser(bean);
            userDAO.setUserOnline(user.getId(), true);
            return true;
        }
        return false;
    }

    public void logout() {
        UserSession session = UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            try {
                userDAO.setUserOnline(session.getUser().getId(), false);
                UserSession.cleanSession();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}