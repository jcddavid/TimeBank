package com.progetto.timebank.model;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String skills;
    private int balance;
    private boolean isOnline;
    private double averageRating;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { this.isOnline = online; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}