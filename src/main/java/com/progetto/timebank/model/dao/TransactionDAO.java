package com.progetto.timebank.model.dao;

import com.progetto.timebank.bean.BookedLessonBean;
import com.progetto.timebank.bean.RequestBean;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.InsufficientBalanceException;
import com.progetto.timebank.exception.PersistenceException;

import java.sql.Timestamp;
import java.util.List;

public interface TransactionDAO {

    void bookLesson(int studentId, int tutorId, String subject)
            throws PersistenceException, InsufficientBalanceException, DuplicateBookingException;

    List<BookedLessonBean> getLessonsByStudentId(int studentId) throws PersistenceException;

    List<RequestBean> getPendingRequestsByTutorId(int tutorId) throws PersistenceException;

    void updateTransactionStatus(int transactionId, String newStatus) throws PersistenceException;

    void finalizeLesson(int transId, int studentId, int tutorId, int hoursCost) throws PersistenceException;

    void submitRating(int transactionId, int rating) throws PersistenceException;

    Timestamp startLessonIfNotStarted(int transId) throws PersistenceException;

    Timestamp getLessonStartTime(int transId) throws PersistenceException;
}










