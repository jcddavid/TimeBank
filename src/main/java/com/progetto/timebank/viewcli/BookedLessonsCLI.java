package com.progetto.timebank.viewcli;

import com.progetto.timebank.bean.BookedLessonBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.BookedLessonsController;
import com.progetto.timebank.exception.PersistenceException;

import java.util.List;
import java.util.Scanner;

public class BookedLessonsCLI {

    public void start(Scanner scanner) {
        BookedLessonsController controller = new BookedLessonsController();
        int myId = UserSession.getInstance().getUser().getId();

        while (true) {
            System.out.println("\n--- LE MIE PRENOTAZIONI ---");
            try {
                List<BookedLessonBean> lessons = controller.getStudentLessons(myId);

                if (lessons.isEmpty()) {
                    System.out.println("Nessuna prenotazione attiva.");
                    System.out.println("Premi INVIO per tornare indietro.");
                    scanner.nextLine();
                    return;
                }

                System.out.printf("%-5s | %-15s | %-12s | %-15s%n", "ID", "Tutor", "Stato", "Disponibilità");
                System.out.println("----------------------------------------------------------");
                for (BookedLessonBean b : lessons) {
                    System.out.printf("%-5d | %-15s | %-12s | %-15s%n",
                            b.getTransactionId(), b.getTutorName(), b.getStatus(), getAvailabilityText(b));
                }

                System.out.print("\nID per Chat (0 esci): ");
                String input = scanner.nextLine().trim();

                if (!input.isEmpty()) {
                    int id = parseId(input);

                    if (id == 0) return;

                    if (id != -1) {
                        BookedLessonBean selected = lessons.stream()
                                .filter(l -> l.getTransactionId() == id)
                                .findFirst()
                                .orElse(null);
                        handleSelected(selected, scanner);
                    }
                }

            } catch (PersistenceException e) {
                System.err.println("Errore nel recupero dati: " + e.getMessage());
                return;
            }
        }
    }

    private int parseId(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void handleSelected(BookedLessonBean selected, Scanner scanner) {
        if (selected != null) {
            boolean isAccepted = "ACCEPTED".equalsIgnoreCase(selected.getStatus()) ||
                    "IN_PROGRESS".equalsIgnoreCase(selected.getStatus());

            if (!isAccepted) {
                System.out.println("❌ IMPOSSIBILE ENTRARE: La lezione non è stata ancora accettata.");
            } else if (!selected.isTutorOnline()) {
                System.out.println("❌ IMPOSSIBILE ENTRARE: Il Tutor è OFFLINE.");
            } else {
                new ChatCLI(selected.getTransactionId(), selected.getPartnerId(), selected.getDuration(), true, scanner).start();
            }
        } else {
            System.out.println(">> ID non trovato.");
        }
    }

    private String getAvailabilityText(BookedLessonBean b) {
        if ("ACCEPTED".equalsIgnoreCase(b.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(b.getStatus())) {
            return b.isTutorOnline() ? "ONLINE (Entra)" : "OFFLINE";
        }
        return "-";
    }
}