package com.progetto.timebank.bean;

public class UserSession {

    private static UserSession instance;
    private UserBean user;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public UserBean getUser() {
        return user;
    }

    public static void cleanSession() {
        instance = null;
    }
}
