package com.progetto.timebank.controller;

import com.progetto.timebank.bean.BookedLessonBean;
import com.progetto.timebank.bean.RequestBean;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.TransactionDAO;
import com.progetto.timebank.exception.PersistenceException;

import java.util.List;

public class BookedLessonsController {

    public List<BookedLessonBean> getStudentLessons(int studentId) throws PersistenceException {
        TransactionDAO dao = DAOFactory.getTransactionDAO();
        return dao.getLessonsByStudentId(studentId);
    }

    public List<RequestBean> getTutorRequests(int tutorId) throws PersistenceException {
        TransactionDAO dao = DAOFactory.getTransactionDAO();
        return dao.getPendingRequestsByTutorId(tutorId);
    }
}