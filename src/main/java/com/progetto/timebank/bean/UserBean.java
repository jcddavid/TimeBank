package com.progetto.timebank.bean;

import com.progetto.timebank.model.User;

public class UserBean {
    private int id;
    private String username;
    private String email;
    private String password;
    private int balance;
    private String skills;
    private double rating;

    public static UserBean fromUser(User user) {
        UserBean bean = new UserBean();
        bean.setId(user.getId());
        bean.setUsername(user.getUsername());
        bean.setEmail(user.getEmail());
        bean.setBalance(user.getBalance());
        bean.setSkills(user.getSkills());
        bean.setRating(user.getAverageRating());
        return bean;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getFormattedRating() {
        if (rating == 0) return "N/A";
        return String.format("%.1f ★", rating);
    }
}