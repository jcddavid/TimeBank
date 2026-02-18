package com.progetto.timebank;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.config.AppConfig;
import com.progetto.timebank.controller.BookingController;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.InsufficientBalanceException;
import com.progetto.timebank.model.User;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BookingLogicTest {

    private BookingController bookingController;

    private final String subjectJDBC = "Test JDBC Integration";
    private final String subjectCSV = "Test CSV Integration";

    // File path transazioni
    private final String csvTransPath = "transactions.csv";

    // Email fisse per testing
    private final String emailSTU = "tester_stu@test.com";
    private final String emailTUT = "tester_tut@test.com";

    // Variabili per gli ID generati dal DB
    private int studentId;
    private int tutorId;

    @BeforeEach
    void globalSetUp() {
        UserSession.cleanSession();
        // Pulizia preventiva totale
        cleanupCSVFile();
    }

    @AfterEach
    void globalTearDown() throws SQLException {
        deleteSpecificTestUsers(); // Pulisce MySQL
        cleanupCSVFile();          // Pulisce CSV
        UserSession.cleanSession();
    }

    @Test
    void testJDBC_InsufficientBalance() throws SQLException {
        setupEnvironment(false); // FALSE = Usa JDBC per le transazioni

        // Imposta saldo 0 su DB
        updateMySQLBalance(studentId, 0);

        assertThrows(InsufficientBalanceException.class, () -> {
            bookingController.bookLesson(studentId, tutorId, subjectJDBC);
        }, "JDBC: Deve bloccare prenotazione con saldo 0");
    }

    @Test
    void testJDBC_DuplicateBooking() throws SQLException {
        setupEnvironment(false); // FALSE = Usa JDBC

        // Imposta saldo 50 su DB
        updateMySQLBalance(studentId, 50);

        // 1. Prima prenotazione (OK) -> Scrive su MySQL
        assertDoesNotThrow(() -> bookingController.bookLesson(studentId, tutorId, subjectJDBC));

        // 2. Seconda prenotazione (Errore) -> Legge da MySQL
        assertThrows(DuplicateBookingException.class, () -> {
            bookingController.bookLesson(studentId, tutorId, subjectJDBC);
        }, "JDBC: Deve bloccare duplicati usando il DB");
    }

    @Test
    void testCSV_InsufficientBalance() throws SQLException {
        setupEnvironment(true);

        updateMySQLBalance(studentId, 0);

        assertThrows(InsufficientBalanceException.class, () -> {
            bookingController.bookLesson(studentId, tutorId, subjectCSV);
        }, "CSV: Deve bloccare se l'utente DB ha saldo 0");
    }

    @Test
    void testCSV_DuplicateBooking() throws SQLException {
        setupEnvironment(true);

        updateMySQLBalance(studentId, 50);

        assertDoesNotThrow(() -> {
            bookingController.bookLesson(studentId, tutorId, subjectCSV);
        }, "CSV: Scrittura su file fallita");

        File f = new File(csvTransPath);
        assertTrue(f.exists(), "Il file transactions.csv deve esistere");

        assertThrows(DuplicateBookingException.class, () -> {
            bookingController.bookLesson(studentId, tutorId, subjectCSV);
        }, "CSV: Deve bloccare duplicati rileggendo il file");
    }

    private void setupEnvironment(boolean useCsvForTransactions) throws SQLException {
        AppConfig.setIsCSV(useCsvForTransactions);

        bookingController = new BookingController();

        deleteSpecificTestUsers(); // Pulisci vecchi
        this.studentId = createTemporaryUserInDB("TestStu", emailSTU);
        this.tutorId = createTemporaryUserInDB("TestTut", emailTUT);

        if (useCsvForTransactions) {
            cleanupCSVFile();
        }
    }

    private int createTemporaryUserInDB(String username, String email) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String sql = "INSERT INTO Users (username, email, password, skills, balance, is_online) VALUES (?, ?, 'pass', 'None', 0, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                else throw new SQLException("ID non generato.");
            }
        }
    }

    private void deleteSpecificTestUsers() throws SQLException {
        Connection conn = DBConnection.getInstance();

        String sqlTrans = "DELETE FROM transactions WHERE student_id IN (SELECT user_id FROM Users WHERE email IN (?, ?))";
        try (PreparedStatement stmt = conn.prepareStatement(sqlTrans)) {
            stmt.setString(1, emailSTU);
            stmt.setString(2, emailTUT);
            stmt.executeUpdate();
        }

        String sqlUsers = "DELETE FROM Users WHERE email IN (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUsers)) {
            stmt.setString(1, emailSTU);
            stmt.setString(2, emailTUT);
            stmt.executeUpdate();
        }
    }

    private void updateMySQLBalance(int userId, int balance) throws SQLException {
        Connection conn = DBConnection.getInstance();
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Users SET balance = ? WHERE user_id = ?")) {
            stmt.setInt(1, balance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }

        User u = DAOFactory.getUserDAO().getUserById(userId);
        if (u != null) {
            UserBean bean = new UserBean();
            bean.setId(u.getId());
            bean.setUsername(u.getUsername());
            bean.setEmail(u.getEmail());
            bean.setBalance(balance);

            UserSession.getInstance().setUser(bean);
        }
    }

    private void cleanupCSVFile() {
        File file = new File(csvTransPath);
        if (!file.exists()) return;
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> cleanLines = lines.stream()
                    .filter(line -> !line.contains(subjectCSV))
                    .toList();
            Files.write(file.toPath(), cleanLines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}