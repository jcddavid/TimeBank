package com.progetto.timebank;

import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.LoginController;
import com.progetto.timebank.model.dao.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class LoginFlowTest {

    private LoginController loginController;

    // Dati dell'utente di test che inseriremo nel Database
    private final String testUser = "TestLoginUser";
    private final String testEmail = "loginflow@test.com";
    private final String testPass = "passwordSicura123";

    @BeforeEach
    void setUp() throws SQLException {
        loginController = new LoginController();
        UserSession.cleanSession();

        // 1. Assicuriamoci che l'utente non esista già
        deleteTestUser();

        // 2. Creazione Utente Reale nel DB MySQL
        createTestUserInDB();
    }

    @AfterEach
    void tearDown() throws SQLException {
        deleteTestUser();
        UserSession.cleanSession();
    }

    @Test
    void testLoginSuccess() throws SQLException {
        boolean result = loginController.login(testUser, testPass);

        assertTrue(result, "Il login dovrebbe restituire TRUE con credenziali corrette salvate nel DB.");

        assertNotNull(UserSession.getInstance().getUser(), "La sessione utente non deve essere null dopo il login.");
        assertEquals(testUser, UserSession.getInstance().getUser().getUsername(), "Lo username in sessione deve corrispondere.");
        assertEquals(testEmail, UserSession.getInstance().getUser().getEmail(), "L'email in sessione deve corrispondere.");
    }

    @Test
    void testLoginFailure_WrongPassword() throws SQLException {
        boolean result = loginController.login(testUser, "passwordSbagliata");
        assertFalse(result, "Il login deve restituire FALSE se la password è errata.");

        assertNull(UserSession.getInstance().getUser(), "La sessione deve rimanere vuota se il login fallisce.");
    }

    @Test
    void testLoginFailure_UserNotFound() throws SQLException {
        boolean result = loginController.login("UtenteFantasma", "qualsiasi");

        assertFalse(result, "Il login deve fallire se l'utente non esiste.");
        assertNull(UserSession.getInstance().getUser());
    }

    @Test
    void testLogout() throws SQLException {
        loginController.login(testUser, testPass);
        assertNotNull(UserSession.getInstance().getUser(), "Prerequisito: login effettuato.");

        loginController.logout();
        assertNull(UserSession.getInstance().getUser(), "Dopo il logout, la sessione deve tornare null.");
    }

    private void createTestUserInDB() throws SQLException {
        Connection conn = DBConnection.getInstance();
        String sql = "INSERT INTO Users (username, email, password, skills, balance, is_online) VALUES (?, ?, ?, 'TestSkill', 10, 0)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, testUser);
            stmt.setString(2, testEmail);
            stmt.setString(3, testPass);
            stmt.executeUpdate();
        }
    }

    private void deleteTestUser() throws SQLException {
        Connection conn = DBConnection.getInstance();
        String sql = "DELETE FROM Users WHERE username = ? OR email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, testUser);
            stmt.setString(2, testEmail);
            stmt.executeUpdate();
        }
    }
}