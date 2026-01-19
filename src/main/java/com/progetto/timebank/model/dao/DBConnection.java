package com.progetto.timebank.model.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
    private static Connection connection = null;
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private DBConnection() {}

    public static Connection getInstance() {
        if (connection == null) {
            try {
                Properties props = new Properties();
                try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (input == null) {
                        LOGGER.log(Level.SEVERE, "Impossibile trovare il file db.properties");
                        return null;
                    }
                    props.load(input);
                }

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                connection = DriverManager.getConnection(url, user, password);

            } catch (IOException | SQLException e) {
                LOGGER.log(Level.SEVERE, "Errore di connessione al Database!", e);
            }
        }
        return connection;
    }
}