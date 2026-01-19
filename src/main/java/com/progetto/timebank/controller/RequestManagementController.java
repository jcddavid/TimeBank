package com.progetto.timebank.controller;

import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.exception.PersistenceException;

public class RequestManagementController {

    public void acceptRequest(int transactionId) throws PersistenceException {
        DAOFactory.getTransactionDAO().updateTransactionStatus(transactionId, "ACCEPTED");
    }

    public void rejectRequest(int transactionId) throws PersistenceException {
        DAOFactory.getTransactionDAO().updateTransactionStatus(transactionId, "REJECTED");
    }
}