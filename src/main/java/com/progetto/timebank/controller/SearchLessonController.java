package com.progetto.timebank.controller;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.UserDAO;
import com.progetto.timebank.exception.PersistenceException;
import com.progetto.timebank.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchLessonController {

    public List<UserBean> searchTutorsBySubject(String subject, int excludeUserId) throws PersistenceException {
        UserDAO dao = DAOFactory.getUserDAO();
        try {
            List<User> users = dao.searchUsersBySkill(subject, excludeUserId);

            List<UserBean> beans = new ArrayList<>();
            for (User u : users) {
                beans.add(UserBean.fromUser(u));
            }
            return beans;
        } catch (SQLException e) {
            throw new PersistenceException("Errore durante la ricerca tutor.", e);
        }
    }
}