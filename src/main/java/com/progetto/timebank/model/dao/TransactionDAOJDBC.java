package com.progetto.timebank.model.dao;

import com.progetto.timebank.bean.BookedLessonBean;
import com.progetto.timebank.bean.RequestBean;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.InsufficientBalanceException;
import com.progetto.timebank.exception.PersistenceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOJDBC implements TransactionDAO {

    @Override
    public void bookLesson(int studentId, int tutorId, String subject)
            throws PersistenceException, InsufficientBalanceException, DuplicateBookingException {

        try {
            Connection conn = DBConnection.getInstance();
            if (getUserBalance(studentId, conn) < 1) {
                throw new InsufficientBalanceException("Saldo insufficiente (JDBC).");
            }
            if (isRequestPending(studentId, tutorId, subject, conn)) {
                throw new DuplicateBookingException("Richiesta già esistente (JDBC).");
            }

            String query = "INSERT INTO transactions (student_id, tutor_id, subject, hours_amount, status, date_time) VALUES (?, ?, ?, 1, 'PENDING', NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, tutorId);
                stmt.setString(3, subject);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Errore Database durante la prenotazione.", e);
        }
    }

    @Override
    public List<BookedLessonBean> getLessonsByStudentId(int studentId) throws PersistenceException {
        try {
            Connection conn = DBConnection.getInstance();
            List<BookedLessonBean> list = new ArrayList<>();
            String query = "SELECT t.trans_id, t.subject, t.date_time, t.status, t.hours_amount, u.username, u.is_online, u.user_id as tutor_id " +
                    "FROM transactions t JOIN Users u ON t.tutor_id = u.user_id WHERE t.student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, studentId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    BookedLessonBean bean = new BookedLessonBean(
                            rs.getString("subject"), rs.getString("username"),
                            rs.getString("date_time"), rs.getString("status"),
                            rs.getInt("is_online") == 1
                    );
                    bean.setDuration(rs.getInt("hours_amount"));
                    bean.setTransactionId(rs.getInt("trans_id"));
                    bean.setPartnerId(rs.getInt("tutor_id"));
                    list.add(bean);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new PersistenceException("Errore nel recupero delle lezioni.", e);
        }
    }

    @Override
    public List<RequestBean> getPendingRequestsByTutorId(int tutorId) throws PersistenceException {
        try {
            Connection conn = DBConnection.getInstance();
            List<RequestBean> list = new ArrayList<>();
            String query = "SELECT t.trans_id, t.subject, t.status, t.hours_amount, u.username, u.is_online, u.user_id as student_id " +
                    "FROM transactions t JOIN Users u ON t.student_id = u.user_id " +
                    "WHERE t.tutor_id = ? AND (t.status = 'PENDING' OR t.status = 'ACCEPTED' OR t.status = 'IN_PROGRESS')";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, tutorId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    RequestBean bean = new RequestBean(
                            rs.getInt("trans_id"), rs.getString("username"),
                            rs.getString("subject"), rs.getString("status"),
                            rs.getInt("is_online") == 1
                    );
                    bean.setDuration(rs.getInt("hours_amount"));
                    bean.setPartnerId(rs.getInt("student_id"));
                    list.add(bean);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new PersistenceException("Errore nel recupero delle richieste.", e);
        }
    }

    @Override
    public void updateTransactionStatus(int transactionId, String newStatus) throws PersistenceException {
        try {
            Connection conn = DBConnection.getInstance();
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE transactions SET status = ? WHERE trans_id = ?")) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, transactionId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Errore aggiornamento stato transazione.", e);
        }
    }

    @Override
    public void finalizeLesson(int transId, int studentId, int tutorId, int hoursCost) throws PersistenceException {
        Connection conn = DBConnection.getInstance();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement s1 = conn.prepareStatement("UPDATE Users SET balance = balance - ? WHERE user_id = ?")) {
                s1.setInt(1, hoursCost);
                s1.setInt(2, studentId);
                s1.executeUpdate();
            }
            try (PreparedStatement s2 = conn.prepareStatement("UPDATE Users SET balance = balance + ? WHERE user_id = ?")) {
                s2.setInt(1, hoursCost);
                s2.setInt(2, tutorId);
                s2.executeUpdate();
            }
            try (PreparedStatement s3 = conn.prepareStatement("UPDATE transactions SET status = 'COMPLETED' WHERE trans_id = ?")) {
                s3.setInt(1, transId);
                s3.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new PersistenceException("Errore durante la finalizzazione della lezione.", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void submitRating(int transactionId, int rating) throws PersistenceException {
        try {
            Connection conn = DBConnection.getInstance();
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE transactions SET rating = ? WHERE trans_id = ?")) {
                stmt.setInt(1, rating);
                stmt.setInt(2, transactionId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Errore salvataggio voto.", e);
        }
    }

    @Override
    public Timestamp startLessonIfNotStarted(int transId) throws PersistenceException {
        try {
            Connection conn = DBConnection.getInstance();
            String checkQuery = "SELECT start_time FROM transactions WHERE trans_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                stmt.setInt(1, transId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Timestamp existingStart = rs.getTimestamp("start_time");
                    if (existingStart != null) return existingStart;
                }
            }
            String updateQuery = "UPDATE transactions SET start_time = NOW(), status = 'IN_PROGRESS' WHERE trans_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, transId);
                stmt.executeUpdate();
            }
            return new Timestamp(System.currentTimeMillis());
        } catch (SQLException e) {
            throw new PersistenceException("Errore avvio lezione DB.", e);
        }
    }

    @Override
    public Timestamp getLessonStartTime(int transId) throws PersistenceException {
        try {
            Connection conn = DBConnection.getInstance();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT start_time FROM transactions WHERE trans_id = ?")) {
                stmt.setInt(1, transId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getTimestamp("start_time");
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenceException("Errore lettura start time.", e);
        }
    }

    // Helpers (Privati, non serve wrap)
    private int getUserBalance(int userId, Connection conn) throws SQLException {
        String sql = "SELECT balance FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("balance");
        }
        return 0;
    }

    private boolean isRequestPending(int studentId, int tutorId, String subject, Connection conn) throws SQLException {
        String sql = "SELECT count(*) FROM transactions WHERE student_id = ? AND tutor_id = ? AND subject = ? AND status = 'PENDING'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, tutorId);
            stmt.setString(3, subject);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }
}