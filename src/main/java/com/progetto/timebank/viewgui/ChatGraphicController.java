package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.ChatController;
import com.progetto.timebank.exception.PersistenceException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;

public class ChatGraphicController {
    @FXML private Label lblTitle;
    @FXML private Label lblTimer;
    @FXML private TextArea txtAreaChat;
    @FXML private TextField txtInput;

    private static final String JOINSTR = "JOIN:";
    private static Stage activeStage = null;
    private static ChatGraphicController activeController = null;

    private Socket socket;
    private PrintWriter writer;

    private boolean isConnected = false;
    private boolean isTimerRunning = false;

    private int transactionId;
    private int remoteUserId;
    private int durationHours;
    private boolean isStudent;

    private Timeline timeline;
    private int secondsRemaining;

    private final ChatController chatController = new ChatController();

    public static void registerStage(Stage stage, ChatGraphicController controller) {
        activeStage = stage;
        activeController = controller;
        stage.setOnCloseRequest(e -> closeActiveSession());
    }

    public static void closeActiveSession() {
        if (activeController != null) activeController.disconnect();
        if (activeStage != null) activeStage.close();
        activeController = null;
        activeStage = null;
    }

    public void setupLesson(int transId, int remoteId, int dur, boolean student, String subject, String partnerName) {
        this.transactionId = transId;
        this.remoteUserId = remoteId;
        this.durationHours = dur;
        this.isStudent = student;

        // Reset stato
        this.isTimerRunning = false;

        if (activeStage != null) {
            activeStage.setTitle("TimeBank Chat - " + subject);
        }

        if (isStudent) {
            lblTitle.setText("Tutor: " + partnerName);
        } else {
            lblTitle.setText("Studente: " + partnerName);
        }

        lblTimer.setText("Caricamento...");

        checkIfLessonAlreadyStarted();
        new Thread(this::connectToServer).start();
    }

    private void checkIfLessonAlreadyStarted() {
        try {
            Timestamp start = chatController.getLessonStartTime(transactionId);

            if (start != null) {
                long elapsedSeconds = (System.currentTimeMillis() - start.getTime()) / 1000;
                int totalDurationSeconds = 60; // DEMO: 60 secondi

                this.secondsRemaining = totalDurationSeconds - (int) elapsedSeconds;

                Platform.runLater(() -> {
                    txtAreaChat.appendText(">>> Bentornato! La lezione è IN CORSO. <<<\n");
                    startCountdown();
                });
            } else {
                Platform.runLater(() -> {
                    String waitingWho = isStudent ? "DEL TUTOR" : "DELLO STUDENTE";
                    txtAreaChat.appendText("--- IN ATTESA DI CONNESSIONE " + waitingWho + " ---\n");
                    lblTimer.setText("00:00:60");
                });
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    private boolean handleCmdStart(String msg) {
        boolean result = false;
        if (msg.startsWith("CMD:START") || msg.startsWith(JOINSTR)) {
            result = true;
            if (!isTimerRunning) {
                txtAreaChat.appendText(">>> Connessione stabilita. Inizio Lezione! <<<\n");
                startCountdown();
            } else {
                if (msg.startsWith(JOINSTR))
                    txtAreaChat.appendText(">>> L'altro utente si è connesso alla chat. <<<\n");
            }
        }
        return result;
    }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 12345);
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
            writer.println(JOINSTR + transactionId + ":" + UserSession.getInstance().getUser().getUsername());

            String msg;
            while ((msg = reader.readLine()) != null) {
                String finalMsg = msg;
                Platform.runLater(() -> {
                    if (handleCmdStart(finalMsg)) {
                        return;
                    }
                    if (finalMsg.startsWith("SYSTEM:")) {
                        String cleanMsg = finalMsg.replace("SYSTEM:", "").trim();
                        if (!cleanMsg.isEmpty()) txtAreaChat.appendText(">>> " + cleanMsg + " <<<\n");
                        return;
                    }

                    txtAreaChat.appendText(finalMsg + "\n");
                });
            }
        } catch (IOException e) {
            if (isConnected) Platform.runLater(() -> txtAreaChat.appendText("Disconnesso dalla chat (Timer continua).\n"));
        }
    }

    private void startCountdown() {
        if (isTimerRunning) return;
        isTimerRunning = true;

        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }

        if (secondsRemaining <= 0 || secondsRemaining == 60) {
            try {
                Timestamp start = chatController.startLesson(transactionId);
                long elapsed = (System.currentTimeMillis() - start.getTime()) / 1000;
                secondsRemaining = 60 - (int) elapsed;
            } catch (Exception e) {
                secondsRemaining = 60;
            }
        }

        if (secondsRemaining <= 0) {
            lblTimer.setText("00:00:00");
            finishLesson();
            return;
        }

        lblTimer.setText(String.format("00:00:%02d", secondsRemaining));

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            if (secondsRemaining >= 0) {
                lblTimer.setText(String.format("00:00:%02d", secondsRemaining));
            }
            if (secondsRemaining <= 0) {
                timeline.stop();
                finishLesson();
            }
        }));
        timeline.setCycleCount(secondsRemaining + 1);
        timeline.play();
    }

    private void finishLesson() {
        if (txtAreaChat != null) txtAreaChat.appendText("\n--- LEZIONE CONCLUSA ---\n");
        if (txtInput != null) txtInput.setDisable(true);
        disconnect();
        if (isStudent) {
            try {
                chatController.finalizeLesson(transactionId, remoteUserId, durationHours);
                Platform.runLater(this::openRatingWindow);
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onSendClick() {
        String msg = txtInput.getText();
        if (msg == null || msg.trim().isEmpty() || writer == null) return;
        writer.println(msg);
        txtAreaChat.appendText("Io: " + msg + "\n");
        txtInput.clear();
    }

    private void openRatingWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Rating.fxml"));
            Parent root = loader.load();
            ((RatingGraphicController)loader.getController()).setTransactionId(transactionId);
            Stage s = new Stage();
            s.setScene(new Scene(root));
            s.show();
            if (activeStage != null) activeStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        isConnected = false;
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isTimerRunning = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}