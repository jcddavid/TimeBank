package com.progetto.timebank.viewcli;

import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.DashboardController;
import com.progetto.timebank.controller.LoginController;

import java.util.Scanner;

public class DashboardCLI {

    private final DashboardController dashboardController = new DashboardController();
    private final LoginController loginController = new LoginController();

    public void start(Scanner scanner) {
        boolean loggedIn = true;

        while (loggedIn) {
            dashboardController.refreshSessionData();

            String user = UserSession.getInstance().getUser().getUsername();
            int balance = UserSession.getInstance().getUser().getBalance();

            System.out.println("\n========================================");
            System.out.println("   DASHBOARD - " + user.toUpperCase());
            System.out.println("   Saldo attuale: " + balance + " ore");
            System.out.println("========================================");
            System.out.println("1. Cerca Tutor e Prenota");
            System.out.println("2. Le mie Offerte (Gestisci Competenze)");
            System.out.println("3. Le mie Prenotazioni (Studente) -> [Chat]");
            System.out.println("4. Richieste Ricevute (Tutor) -> [Chat]");
            System.out.println("5. Logout");
            System.out.print("Scegli un'opzione: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> new SearchCLI().start(scanner);
                case "2" -> new MyOffersCLI().start(scanner);
                case "3" -> new BookedLessonsCLI().start(scanner);
                case "4" -> new RequestsCLI().start(scanner);
                case "5" -> {
                    loginController.logout();
                    loggedIn = false;
                    System.out.println("Logout effettuato. A presto!");
                }
                default -> System.out.println("Comando non valido, riprova.");
            }
        }
    }
}