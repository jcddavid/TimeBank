package com.progetto.timebank.model.dao;

import com.progetto.timebank.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    User getUserByUsernameAndPassword(String username, String password) throws SQLException;
    User getUserById(int userId) throws SQLException;
    boolean checkUserExists(String username, String email) throws SQLException;
    void registerUser(User user) throws SQLException;
    void setUserOnline(int userId, boolean isOnline) throws SQLException;
    void updateUserSkills(int userId, String skills) throws SQLException;
    double getAverageRating(int userId) throws SQLException;
    List<User> searchUsersBySkill(String skill, int excludeUserId) throws SQLException;
    boolean isUserOnline(int userId);
}