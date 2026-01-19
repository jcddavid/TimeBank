package com.progetto.timebank.controller;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.UserDAO;
import com.progetto.timebank.model.User;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {

    public void registerUser(UserBean bean) throws SQLException, IllegalArgumentException {

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (bean.getEmail() == null || !Pattern.matches(emailRegex, bean.getEmail())) {
            throw new IllegalArgumentException("Il formato dell'email non è valido.");
        }

        if (bean.getPassword() == null || bean.getPassword().length() <= 7) {
            throw new IllegalArgumentException("La password deve essere lunga almeno 8 caratteri.");
        }

        if (bean.getUsername() == null || bean.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Lo username non può essere vuoto.");
        }

        // USA LA FACTORY
        UserDAO dao = DAOFactory.getUserDAO();

        if (dao.checkUserExists(bean.getUsername(), bean.getEmail())) {
            throw new IllegalArgumentException("Username o Email già utilizzati.");
        }

        User user = new User();
        user.setUsername(bean.getUsername());
        user.setEmail(bean.getEmail());
        user.setPassword(bean.getPassword());

        dao.registerUser(user);
    }
}