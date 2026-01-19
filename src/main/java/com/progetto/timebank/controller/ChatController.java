package com.progetto.timebank.controller;

import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.TransactionDAO;
import com.progetto.timebank.model.dao.UserDAO;
import com.progetto.timebank.exception.PersistenceException;
import com.progetto.timebank.model.User;
import java.sql.Timestamp;

public class ChatController {

    public Timestamp getLessonStartTime(int transId) throws PersistenceException {
        return DAOFactory.getTransactionDAO().getLessonStartTime(transId);
    }

    public Timestamp startLesson(int transId) throws PersistenceException {
        return DAOFactory.getTransactionDAO().startLessonIfNotStarted(transId);
    }

    public void finalizeLesson(int transId, int targetUserId, int duration) throws PersistenceException {
        int myId = UserSession.getInstance().getUser().getId();
        TransactionDAO dao = DAOFactory.getTransactionDAO();

        dao.finalizeLesson(transId, myId, targetUserId, duration);

        refreshSessionBalance(myId);
    }

    private void refreshSessionBalance(int userId) {
        try {
            UserDAO dao = DAOFactory.getUserDAO();
            User u = dao.getUserById(userId);
            if(u != null) UserSession.getInstance().getUser().setBalance(u.getBalance());
        } catch (Exception e) { e.printStackTrace(); }
    }
}