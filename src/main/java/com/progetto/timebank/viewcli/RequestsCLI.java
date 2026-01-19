package com.progetto.timebank.viewcli;

import com.progetto.timebank.bean.RequestBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.BookedLessonsController;
import com.progetto.timebank.controller.RequestManagementController;
import com.progetto.timebank.exception.PersistenceException;

import java.util.List;
import java.util.Scanner;

public class RequestsCLI {

    private final BookedLessonsController listController = new BookedLessonsController();
    private final RequestManagementController actionController = new RequestManagementController();

    public void start(Scanner scanner) {
        int myId = UserSession.getInstance().getUser().getId();
        boolean running = true;

        while (running) {
            running = processLoopStep(scanner, myId);
        }
    }

    private boolean processLoopStep(Scanner scanner, int myId) {
        System.out.println("\n--- RICHIESTE RICEVUTE ---");
        try {
            List<RequestBean> requests = listController.getTutorRequests(myId);

            if (requests.isEmpty()) {
                System.out.println("Nessuna richiesta.");
                System.out.println("Premi INVIO per uscire.");
                scanner.nextLine();
                return false;
            }

            printRequests(requests);

            System.out.print("\nID da gestire (0 esci): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return true;

            int id = parseId(input);
            if (id == 0) return false;
            if (id == -1) return true;

            RequestBean selected = requests.stream()
                    .filter(r -> r.getTransactionId() == id)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                handleSelection(selected, scanner);
            }
            return true;

        } catch (PersistenceException e) {
            System.out.println("Errore sistema: " + e.getMessage());
            scanner.nextLine();
            return false;
        }
    }

    private void printRequests(List<RequestBean> requests) {
        for (RequestBean req : requests) {
            System.out.printf("ID: %d | Studente: %s | Materia: %s | Stato: %s%n",
                    req.getTransactionId(), req.getStudentName(), req.getSubject(), req.getStatus());
        }
    }

    private int parseId(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void handleSelection(RequestBean selected, Scanner scanner) throws PersistenceException {
        if ("PENDING".equals(selected.getStatus())) {
            processPending(selected, scanner);
        } else if ("ACCEPTED".equals(selected.getStatus())) {
            processAccepted(selected, scanner);
        }
    }

    private void processPending(RequestBean selected, Scanner scanner) throws PersistenceException {
        System.out.print("1. Accetta  2. Rifiuta: ");
        String choice = scanner.nextLine();
        if ("1".equals(choice)) {
            actionController.acceptRequest(selected.getTransactionId());
            System.out.println("Accettata.");
        } else if ("2".equals(choice)) {
            actionController.rejectRequest(selected.getTransactionId());
            System.out.println("Rifiutata.");
        }
    }

    private void processAccepted(RequestBean selected, Scanner scanner) {
        if (selected.isStudentOnline()) {
            System.out.print("Entrare in chat? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                new ChatCLI(selected.getTransactionId(), selected.getPartnerId(), selected.getDuration(), false, scanner).start();
            }
        } else {
            System.out.println("Studente Offline.");
        }
    }
}