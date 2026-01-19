package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.RequestBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.BookedLessonsController;
import com.progetto.timebank.controller.RequestManagementController;
import com.progetto.timebank.exception.PersistenceException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IncomingRequestsGraphicController {

    private static final Logger LOGGER = Logger.getLogger(IncomingRequestsGraphicController.class.getName());

    @FXML private TableView<RequestBean> tableRequests;
    @FXML private TableColumn<RequestBean, String> colStudent;
    @FXML private TableColumn<RequestBean, String> colSubject;
    @FXML private TableColumn<RequestBean, RequestBean> colActions;

    @FXML
    public void initialize() {
        colStudent.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("studentName"));
        colSubject.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subject"));

        colActions.setStyle("-fx-alignment: CENTER;");
        colActions.setCellValueFactory(param -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue()));

        colActions.setCellFactory(param -> new TableCell<RequestBean, RequestBean>() {
            private final Button btnAccept = new Button("Accetta");
            private final Button btnReject = new Button("Rifiuta");
            private final Button btnChat = new Button("Chat");
            private final HBox pane = new HBox(5, btnAccept, btnReject, btnChat);

            @Override
            protected void updateItem(RequestBean req, boolean empty) {
                pane.setAlignment(Pos.CENTER);
                btnAccept.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                btnReject.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                super.updateItem(req, empty);

                if (req == null || empty) {
                    setGraphic(null);
                    return;
                }
                if ("PENDING".equals(req.getStatus())) {
                    configureButton(btnAccept, true);
                    configureButton(btnReject, true);
                    configureButton(btnChat, false);

                    btnAccept.setOnAction(e -> handleRequest(req, true));
                    btnReject.setOnAction(e -> handleRequest(req, false));

                } else if ("ACCEPTED".equals(req.getStatus()) || "IN_PROGRESS".equals(req.getStatus())) {
                    configureButton(btnAccept, false);
                    configureButton(btnReject, false);
                    configureButton(btnChat, true);

                    if (req.isStudentOnline()) {
                        btnChat.setText("Chat");
                        btnChat.setDisable(false);
                        btnChat.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                    } else {
                        btnChat.setText("Studente offline");
                        btnChat.setDisable(true);
                        btnChat.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: black;");
                    }

                    btnChat.setOnAction(e -> openChat(req));
                } else {
                    setGraphic(null);
                    return;
                }

                setGraphic(pane);
            }

            private void configureButton(Button b, boolean visible) {
                b.setVisible(visible);
                b.setManaged(visible);
            }
        });

        loadRequests();
    }

    @FXML protected void onRefreshClick() { loadRequests(); }

    private void loadRequests() {
        UserSession session = UserSession.getInstance();
        if (session == null) return;
        BookedLessonsController controller = new BookedLessonsController();
        try {
            List<RequestBean> list = controller.getTutorRequests(session.getUser().getId());
            tableRequests.setItems(FXCollections.observableArrayList(list));
        } catch (PersistenceException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento richieste", e);
            tableRequests.setPlaceholder(new Label("Errore: " + e.getMessage()));
        }
    }

    private void handleRequest(RequestBean req, boolean accept) {
        RequestManagementController controller = new RequestManagementController();
        try {
            if (accept) controller.acceptRequest(req.getTransactionId());
            else controller.rejectRequest(req.getTransactionId());
            loadRequests();
        } catch (PersistenceException e) {
            LOGGER.log(Level.SEVERE, "Errore gestione richiesta", e);
            new Alert(Alert.AlertType.ERROR, "Errore: " + e.getMessage()).show();
        }
    }

    private void openChat(RequestBean req) {
        try {
            ChatGraphicController.closeActiveSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            Parent root = loader.load();
            ChatGraphicController controller = loader.getController();

            Stage stage = new Stage();
            ChatGraphicController.registerStage(stage, controller);

            controller.setupLesson(
                    req.getTransactionId(),
                    req.getPartnerId(),
                    req.getDuration(),
                    false, // Tutor
                    req.getSubject(),
                    req.getStudentName()
            );

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile aprire la chat", e);
        }
    }
}