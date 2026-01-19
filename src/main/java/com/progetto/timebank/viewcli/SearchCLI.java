package com.progetto.timebank.viewcli;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.BookingController;
import com.progetto.timebank.controller.SearchLessonController;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.InsufficientBalanceException;
import com.progetto.timebank.exception.PersistenceException;
import com.progetto.timebank.model.Course;

import java.util.List;
import java.util.Scanner;

public class SearchCLI {

    private Scanner scanner;
    private final SearchLessonController searchController = new SearchLessonController();
    private final BookingController bookingController = new BookingController();

    public void start(Scanner scanner) {
        this.scanner = scanner;
        boolean running = true;
        while (running) {
            printCourses();
            String input = scanner.nextLine().trim();
            if ("0".equals(input)) {
                running = false;
            } else {
                running = handleCourseSelection(input);
            }
        }
    }

    private void printCourses() {
        System.out.println("\n--- CERCA TUTOR ---");
        int i = 1;
        for (Course c : Course.values()) {
            System.out.println(i + ". " + c);
            i++;
        }
        System.out.print("Numero materia (o 0 per uscire): ");
    }

    private boolean handleCourseSelection(String input) {
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < Course.values().length) {
                return processSearch(idx);
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Input non valido.");
        }
        return true;
    }

    private boolean processSearch(int idx) {
        try {
            String skill = Course.values()[idx].toString();
            int myId = UserSession.getInstance().getUser().getId();
            List<UserBean> tutors = searchController.searchTutorsBySubject(skill, myId);

            if (tutors.isEmpty()) {
                System.out.println(">> Nessun tutor trovato per " + skill);
                return true;
            }
            return showTutorsAndBook(tutors, myId, skill);
        } catch (PersistenceException e) {
            System.err.println("!!! ERRORE DI SISTEMA: " + e.getMessage());
            return false;
        }
    }

    private boolean showTutorsAndBook(List<UserBean> tutors, int myId, String skill) {
        printTutorList(tutors);
        String idStr = scanner.nextLine().trim();
        if (idStr.isEmpty()) return true;

        try {
            int tutorId = Integer.parseInt(idStr);
            if (tutorId != 0) {
                return executeBooking(myId, tutorId, skill);
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Input non valido.");
        }
        return true;
    }

    private void printTutorList(List<UserBean> tutors) {
        System.out.printf("%-5s %-20s %-10s%n", "ID", "Nome", "Rating");
        System.out.println("--------------------------------------");
        for (UserBean t : tutors) {
            System.out.printf("%-5d %-20s %s%n", t.getId(), t.getUsername(), t.getFormattedRating());
        }
        System.out.print("\nID Tutor da prenotare (0 annulla): ");
    }

    private boolean executeBooking(int myId, int tutorId, String skill) {
        try {
            bookingController.bookLesson(myId, tutorId, skill);
            System.out.println(">>> ✅ Prenotazione inviata con successo!");
            return false;
        } catch (InsufficientBalanceException e) {
            System.err.println("!!! ERRORE CREDITO: " + e.getMessage());
        } catch (DuplicateBookingException e) {
            System.err.println("!!! ATTENZIONE: " + e.getMessage());
        } catch (PersistenceException e) {
            System.err.println("!!! ERRORE DI SISTEMA: " + e.getMessage());
            return false;
        }
        return true;
    }
}