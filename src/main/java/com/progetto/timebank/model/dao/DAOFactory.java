package com.progetto.timebank.model.dao;

import com.progetto.timebank.config.AppConfig;

public class DAOFactory {
    private DAOFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static UserDAO getUserDAO() {
        if (AppConfig.getIsDemo()) {
            return new UserDAOMemory();
        } else {
            return new UserDAOJDBC();
        }
    }

    public static TransactionDAO getTransactionDAO() {
        if (AppConfig.getIsDemo()) {
            return new TransactionDAOMemory();
        } else {
            if (AppConfig.getIsCSV()) {
                return new TransactionDAOCSV();
            } else {
                return new TransactionDAOJDBC();
            }
        }
    }
}
