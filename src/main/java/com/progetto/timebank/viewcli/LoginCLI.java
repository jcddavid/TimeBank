package com.progetto.timebank.viewcli;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.controller.LoginController;
import com.progetto.timebank.controller.RegisterController;

import java.util.Scanner;

public class LoginCLI {

    private final Scanner scanner = new Scanner(System.in);
    private final LoginController loginController = new LoginController();
    private final RegisterController registerController = new RegisterController();

    public void start() {
        System.out.println("\n=== BENVENUTO IN TIMEBANK ===");
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("1. Login");
            System.out.println("2. Registrati");
            System.out.println("0. Esci");
            System.out.print("> ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> isRunning = false;
                default -> System.out.println("Comando non valido.");
            }
        }
    }

    private void login() {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pwd = scanner.nextLine();

        try {
            boolean success = loginController.login(user, pwd);

            if (success) {
                System.out.println("Login OK!");
                new DashboardCLI().start(scanner);
            } else {
                System.out.println("Credenziali errate.");
            }
        } catch (Exception e) {
            System.out.println("Errore di sistema durante il login.");
        }
    }

    private void register() {
        System.out.println("\n--- REGISTRAZIONE ---");
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String pwd = scanner.nextLine();

        UserBean newBean = new UserBean();
        newBean.setUsername(user);
        newBean.setEmail(email);
        newBean.setPassword(pwd);

        try {
            registerController.registerUser(newBean);
            System.out.println("Registrazione avvenuta con successo! Ora puoi fare il login.");
        } catch (Exception e) {
            System.out.println("Errore: " + e.getMessage());
        }
    }
}