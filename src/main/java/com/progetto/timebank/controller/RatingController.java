package com.progetto.timebank.controller;

import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.exception.PersistenceException;

public class RatingController {

    public void submitRating(int transactionId, int rating) throws PersistenceException {
        DAOFactory.getTransactionDAO().submitRating(transactionId, rating);
    }
}