package com.progetto.timebank.viewcli;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.MyOffersController;
import com.progetto.timebank.model.Course;
import com.progetto.timebank.exception.PersistenceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MyOffersCLI {

    private final MyOffersController controller = new MyOffersController();

    public void start(Scanner scanner) {

        while (true) {
            UserBean me = UserSession.getInstance().getUser();
            String currentSkills = (me.getSkills() == null || me.getSkills().isEmpty()) ? "Nessuna" : me.getSkills();

            System.out.println("\n--- LE MIE OFFERTE ---");
            System.out.println("Competenze attuali: " + currentSkills);
            System.out.println("----------------------");
            System.out.println("1. Aggiungi competenza");
            System.out.println("2. Rimuovi competenza");
            System.out.println("0. Torna Indietro");
            System.out.print("> ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> addSkill(scanner, me);
                case "2" -> removeSkill(scanner, me);
                case "0" -> { return; }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void addSkill(Scanner scanner, UserBean me) {
        System.out.println("\nScegli una materia:");
        int i = 1;
        for (Course c : Course.values()) {
            System.out.println(i + ". " + c);
            i++;
        }
        System.out.print("Numero: ");
        try {
            String input = scanner.nextLine();
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < Course.values().length) {
                String selected = Course.values()[idx].toString();
                String current = (me.getSkills() == null) ? "" : me.getSkills();

                if (current.contains(selected)) {
                    System.out.println("Hai già questa competenza.");
                    return;
                }

                String updated = current.isEmpty() ? selected : current + ", " + selected;

                controller.updateSkills(me.getId(), updated);

                me.setSkills(updated);
                System.out.println(">>> Competenza aggiunta!");

            } else {
                System.out.println("Numero non valido.");
            }
        } catch (PersistenceException e) {
            System.out.println("Errore aggiornamento: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Input non valido.");
        }
    }

    private void removeSkill(Scanner scanner, UserBean me) {
        if (me.getSkills() == null || me.getSkills().isEmpty()) {
            System.out.println("Non hai competenze da rimuovere.");
            return;
        }

        List<String> skillsList = new ArrayList<>(Arrays.asList(me.getSkills().split(", ")));

        System.out.println("\nQuale vuoi rimuovere?");
        for (int i = 0; i < skillsList.size(); i++) {
            System.out.println((i + 1) + ". " + skillsList.get(i));
        }
        System.out.print("Numero: ");

        try {
            String input = scanner.nextLine();
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < skillsList.size()) {
                skillsList.remove(idx);
                String updated = String.join(", ", skillsList);
                controller.updateSkills(me.getId(), updated);

                me.setSkills(updated);
                System.out.println(">>> Competenza rimossa!");
            } else {
                System.out.println("Numero non valido.");
            }
        } catch (PersistenceException e) {
            System.out.println("Errore aggiornamento: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Input non valido.");
        }
    }
}