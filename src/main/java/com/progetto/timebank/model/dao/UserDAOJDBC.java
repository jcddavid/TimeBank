package com.progetto.timebank.model.dao;

import com.progetto.timebank.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAOJDBC implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOJDBC.class.getName());
    private static final String ALL_COLUMNS = "user_id, username, email, password, skills, balance, is_online";
    private static final String SELECTSTR = "SELECT ";

    @Override
    public User getUserByUsernameAndPassword(String username, String password) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String query = SELECTSTR + ALL_COLUMNS + " FROM Users WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapUserFromResultSet(rs);
        }
        return null;
    }

    @Override
    public User getUserById(int userId) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String query = SELECTSTR + ALL_COLUMNS + " FROM Users WHERE user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapUserFromResultSet(rs);
        }
        return null;
    }

    @Override
    public boolean checkUserExists(String username, String email) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String query = "SELECT count(1) FROM Users WHERE username = ? OR email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public void registerUser(User user) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String query = "INSERT INTO Users (username, email, password, skills, balance, is_online) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getSkills() != null ? user.getSkills() : "");
            stmt.setInt(5, user.getBalance());
            stmt.setInt(6, user.isOnline() ? 1 : 0);
            stmt.executeUpdate();
        }
    }

    @Override
    public void setUserOnline(int userId, boolean isOnline) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String query = "UPDATE Users SET is_online = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, isOnline ? 1 : 0);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateUserSkills(int userId, String skills) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String query = "UPDATE Users SET skills = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, skills);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public double getAverageRating(int userId) throws SQLException {
        Connection conn = DBConnection.getInstance();
        String query = "SELECT AVG(rating) FROM transactions WHERE tutor_id = ? AND rating > 0";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public List<User> searchUsersBySkill(String skill, int excludeUserId) throws SQLException {
        List<User> users = new ArrayList<>();
        Connection conn = DBConnection.getInstance();
        String query = SELECTSTR + ALL_COLUMNS + " FROM Users WHERE skills LIKE ? AND user_id != ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + skill + "%");
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapUserFromResultSet(rs));
            }
        }
        return users;
    }

    @Override
    public boolean isUserOnline(int userId) {
        try {
            Connection conn = DBConnection.getInstance();
            String query = "SELECT is_online FROM Users WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("is_online") == 1;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore controllo stato online utente " + userId);
        }
        return false;
    }

    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setBalance(rs.getInt("balance"));
        user.setSkills(rs.getString("skills"));
        user.setOnline(rs.getInt("is_online") == 1);

        double avg = getAverageRating(user.getId());
        user.setAverageRating(avg);

        return user;
    }
}