package com.progetto.timebank.controller;

import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.TransactionDAO;
import com.progetto.timebank.model.dao.UserDAO;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.InsufficientBalanceException;
import com.progetto.timebank.exception.PersistenceException;
import com.progetto.timebank.model.User;

import java.sql.SQLException;

public class BookingController {

    public void bookLesson(int studentId, int tutorId, String subject)
            throws PersistenceException, InsufficientBalanceException, DuplicateBookingException {

        // 1. Controllo Preliminare del saldo tramite FACTORY
        UserDAO userDAO = DAOFactory.getUserDAO();
        try {
            User student = userDAO.getUserById(studentId);

            if (student == null || student.getBalance() <= 0) {
                throw new InsufficientBalanceException("Il tuo saldo è di " +
                        (student != null ? student.getBalance() : 0) +
                        " ore. Impossibile prenotare.");
            }

        } catch (SQLException e) {
            throw new PersistenceException("Errore durante la verifica del saldo.", e);
        }

        // 2. Procedo con la prenotazione
        TransactionDAO dao = DAOFactory.getTransactionDAO();
        dao.bookLesson(studentId, tutorId, subject);
    }
}