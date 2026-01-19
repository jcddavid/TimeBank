package com.progetto.timebank.viewcli;

import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.ChatController;
import com.progetto.timebank.controller.RatingController;
import com.progetto.timebank.exception.PersistenceException;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatCLI {
    private final int transactionId;
    private final int remoteUserId;
    private final int durationHours;
    private final boolean isStudent;
    private final String myUsername;
    private final Scanner scanner;

    private Socket socket;
    private BufferedReader reader;

    private final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicBoolean timerStarted = new AtomicBoolean(false);

    private final ChatController chatController = new ChatController();
    private final RatingController ratingController = new RatingController();

    public ChatCLI(int transactionId, int remoteUserId, int durationHours, boolean isStudent, Scanner scanner) {
        this.transactionId = transactionId;
        this.remoteUserId = remoteUserId;
        this.durationHours = durationHours;
        this.isStudent = isStudent;
        this.scanner = scanner;
        this.myUsername = UserSession.getInstance().getUser().getUsername();
    }

    public void start() {
        System.out.println("\n==========================================");
        System.out.println("   CHAT LEZIONE (In attesa di connessione...)");
        System.out.println("==========================================");

        try (Socket socketResource = new Socket("127.0.0.1", 12345);
             PrintWriter writer = new PrintWriter(socketResource.getOutputStream(), true);
             BufferedReader readerResource = new BufferedReader(new InputStreamReader(socketResource.getInputStream()))) {

            this.socket = socketResource;
            this.reader = readerResource;

            writer.println("JOIN:" + transactionId + ":" + myUsername);

            Thread listener = new Thread(this::listenForMessages);
            listener.setDaemon(true);
            listener.start();

            while (active.get()) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    if (!active.get() || input.equalsIgnoreCase("exit")) {
                        break;
                    }
                    writer.println(input);
                }
            }

            if (isStudent) {
                finalizeSession();
                askForRating();
            }

        } catch (IOException e) {
            System.err.println("Errore connessione server: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (active.get() && (message = reader.readLine()) != null) {
                if ("CMD:START".equals(message)) {
                    if (timerStarted.compareAndSet(false, true)) {
                        System.out.println("\n>>> UTENTE CONNESSO. IL TIMER (60s) PARTE ORA! <<<");
                        startTimerLogic();
                    }
                    continue;
                }

                if (!message.startsWith("JOIN:")) {
                    System.out.println("\n" + message);
                    System.out.print("> ");
                }
            }
        } catch (IOException e) {
            System.err.println("Connessione persa o interrotta.");
        }
    }

    private void startTimerLogic() {
        try {
            chatController.startLesson(transactionId);
        } catch (Exception e) {
            System.err.println("Errore avvio timer DB: " + e.getMessage());
        }

        new Thread(() -> {
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (active.get()) {
                active.set(false);
                System.out.println("\n\n!!! TEMPO SCADUTO !!! Premi INVIO.");
            }
        }).start();
    }

    private void finalizeSession() {
        try {
            chatController.finalizeLesson(transactionId, remoteUserId, durationHours);
            int current = UserSession.getInstance().getUser().getBalance();
            UserSession.getInstance().getUser().setBalance(current - durationHours);
        } catch (PersistenceException e) {
            System.err.println("Errore finalizzazione lezione: " + e.getMessage());
        }
    }

    private void askForRating() {
        System.out.println("\n--- VOTA IL TUTOR (1-5) ---");
        int rating = 0;

        while (rating < 1 || rating > 5) {
            System.out.print("> ");
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                try {
                    rating = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Input non valido! Riprova!");
                }
            }
        }

        try {
            ratingController.submitRating(transactionId, rating);
            System.out.println("Rating inviato! Grazie.");
        } catch (Exception e) {
            System.err.println("Errore invio rating: " + e.getMessage());
        }
    }

    private void disconnect() {
        active.set(false);
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Errore chiusura socket: " + e.getMessage());
        }
    }
}