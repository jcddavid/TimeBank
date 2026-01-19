package com.progetto.timebank.model.dao;

import com.progetto.timebank.model.dao.memory.MockDatabase;
import com.progetto.timebank.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAOMemory implements UserDAO {

    @Override
    public User getUserByUsernameAndPassword(String username, String password) {
        return MockDatabase.USERS.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User getUserById(int userId) {
        return MockDatabase.USERS.stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean checkUserExists(String username, String email) {
        return MockDatabase.USERS.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username) || u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public void registerUser(User user) {
        user.setId(MockDatabase.getNextUserId());
        user.setBalance(10); // Bonus benvenuto demo
        user.setOnline(true); // DEMO: Sempre online
        MockDatabase.USERS.add(user);
    }

    @Override
    public void setUserOnline(int userId, boolean isOnline) {
        // In modalità DEMO ignoriamo il logout "reale" per mantenere la chat attiva
        // Oppure lo aggiorniamo solo in memoria
        User u = getUserById(userId);
        if (u != null) u.setOnline(true); // Forziamo TRUE in demo
    }

    @Override
    public void updateUserSkills(int userId, String skills) {
        User u = getUserById(userId);
        if (u != null) u.setSkills(skills);
    }

    @Override
    public double getAverageRating(int userId) {
        return MockDatabase.TRANSACTIONS.stream()
                .filter(t -> t.tutorId == userId && t.rating > 0)
                .mapToInt(t -> t.rating)
                .average()
                .orElse(0.0);
    }

    @Override
    public List<User> searchUsersBySkill(String skill, int excludeUserId) {
        List<User> results = new ArrayList<>();
        for (User u : MockDatabase.USERS) {
            if (u.getId() != excludeUserId && u.getSkills() != null && u.getSkills().contains(skill)) {
                u.setAverageRating(getAverageRating(u.getId()));
                results.add(u);
            }
        }
        return results;
    }

    @Override
    public boolean isUserOnline(int userId) {
        // DEMO: Gli utenti sono sempre online
        return true;
    }
}