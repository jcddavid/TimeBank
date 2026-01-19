package com.progetto.timebank;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.config.AppConfig;
import com.progetto.timebank.controller.BookingController;
import com.progetto.timebank.controller.RequestManagementController;
import com.progetto.timebank.model.dao.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class FullCycleTest {

    private BookingController bookingController;
    private RequestManagementController requestController;

    // Dati per il test
    private final String studentEmail = "student_cycle@test.com";
    private final String tutorEmail = "tutor_cycle@test.com";
    private final String subject = "Ciclo System Test";

    private int studentId;
    private int tutorId;

    @BeforeEach
    void setUp() throws SQLException {
        AppConfig.setIsDemo(false);
        AppConfig.setIsCSV(false);

        bookingController = new BookingController();
        requestController = new RequestManagementController();
        UserSession.cleanSession();

        cleanup();
        this.studentId = createUser(studentEmail, "StudentCycle");
        this.tutorId = createUser(tutorEmail, "TutorCycle");

        updateBalance(studentId, 10);
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanup();
        UserSession.cleanSession();
    }

    @Test
    void testBookingAndAcceptanceFlow() throws SQLException {
        // 1. Login simulato Studente
        loginAs(studentId, studentEmail, "StudentCycle");

        // 2. Esecuzione Prenotazione
        assertDoesNotThrow(() -> {
            bookingController.bookLesson(studentId, tutorId, subject);
        }, "Lo studente dovrebbe riuscire a prenotare senza errori.");

        // 3. Verifica Database (Stato PENDING)
        int transId = getTransactionId(studentId, tutorId, subject);
        assertTrue(transId > 0, "La transazione deve essere stata creata nel DB (ID > 0).");
        assertEquals("PENDING", getTransactionStatus(transId), "Lo stato iniziale deve essere PENDING.");

        // 4. Login simulato Tutor
        UserSession.cleanSession();
        loginAs(tutorId, tutorEmail, "TutorCycle");

        // 5. Il Tutor accetta la richiesta
        assertDoesNotThrow(() -> {
            requestController.acceptRequest(transId);
        }, "Il tutor deve poter accettare la richiesta.");

        String finalStatus = getTransactionStatus(transId);
        assertEquals("ACCEPTED", finalStatus, "Lo stato finale nel DB deve essere ACCEPTED.");
    }

    private void loginAs(int userId, String email, String username) {
        UserBean bean = new UserBean();
        bean.setId(userId);
        bean.setEmail(email);
        bean.setUsername(username);
        bean.setBalance(10); // Valore fittizio per la sessione
        UserSession.getInstance().setUser(bean);
    }

    private int createUser(String email, String username) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String sql = "INSERT INTO Users (username, email, password, skills, balance, is_online) VALUES (?, ?, 'pass', 'None', 0, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Impossibile creare utente test nel DB.");
    }

    private void updateBalance(int userId, int amount) throws SQLException {
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Users SET balance = ? WHERE user_id = ?")) {
            stmt.setInt(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private int getTransactionId(int sId, int tId, String subj) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String sql = "SELECT trans_id FROM transactions WHERE student_id = ? AND tutor_id = ? AND subject = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sId);
            stmt.setInt(2, tId);
            stmt.setString(3, subj);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("trans_id");
        }
        return -1;
    }

    private String getTransactionStatus(int transId) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String sql = "SELECT status FROM transactions WHERE trans_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("status");
        }
        return "NOT_FOUND";
    }

    private void cleanup() throws SQLException {
        Connection conn = DBConnection.getInstance();
        // Cancella le transazioni
        String sqlT = "DELETE FROM transactions WHERE student_id IN (SELECT user_id FROM Users WHERE email IN (?, ?))";
        try (PreparedStatement stmt = conn.prepareStatement(sqlT)) {
            stmt.setString(1, studentEmail);
            stmt.setString(2, tutorEmail);
            stmt.executeUpdate();
        }
        // Cancella gli utenti
        String sqlU = "DELETE FROM Users WHERE email IN (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlU)) {
            stmt.setString(1, studentEmail);
            stmt.setString(2, tutorEmail);
            stmt.executeUpdate();
        }
    }
}