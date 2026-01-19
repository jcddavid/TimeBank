package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.BookedLessonBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.BookedLessonsController;
import com.progetto.timebank.exception.PersistenceException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class BookedLessonsGraphicController {

    @FXML private TableView<BookedLessonBean> tableLessons;
    @FXML private TableColumn<BookedLessonBean, String> colDate;
    @FXML private TableColumn<BookedLessonBean, String> colSubject;
    @FXML private TableColumn<BookedLessonBean, String> colTutor;
    @FXML private TableColumn<BookedLessonBean, String> colStatus;
    @FXML private TableColumn<BookedLessonBean, BookedLessonBean> colAction;

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));
        colSubject.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subject"));
        colTutor.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tutorName"));
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        colAction.setStyle("-fx-alignment: CENTER;");
        colAction.setCellValueFactory(param -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue()));

        colAction.setCellFactory(param -> new TableCell<BookedLessonBean, BookedLessonBean>() {
            private final Button btn = new Button("Entra");

            @Override
            protected void updateItem(BookedLessonBean lesson, boolean empty) {
                super.updateItem(lesson, empty);
                if (lesson == null || empty) {
                    setGraphic(null);
                    return;
                }

                setAlignment(Pos.CENTER);
                configureButton(btn, lesson);

                btn.setOnAction(event -> openChat(lesson));
                setGraphic(btn);
            }
        });

        loadData();
    }

    private void configureButton(Button btn, BookedLessonBean lesson) {
        String status = lesson.getStatus();

        if ("COMPLETED".equalsIgnoreCase(status)) {
            btn.setText("Conclusa");
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        }
        else if ("REJECTED".equalsIgnoreCase(status)) {
            btn.setText("Rifiutata");
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        }
        else if ("ACCEPTED".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status)) {
            if (lesson.isTutorOnline()) {
                btn.setText("Entra");
                btn.setDisable(false);
                btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
            } else {
                btn.setText("Tutor offline");
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: black;");
            }
        }
        else {
            btn.setText("In attesa");
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        }
    }

    @FXML protected void onRefreshClick() { loadData(); }

    private void loadData() {
        UserSession session = UserSession.getInstance();
        if (session == null) return;
        BookedLessonsController controller = new BookedLessonsController();
        try {
            List<BookedLessonBean> list = controller.getStudentLessons(session.getUser().getId());
            tableLessons.setItems(FXCollections.observableArrayList(list));
        } catch (PersistenceException e) {
            tableLessons.setPlaceholder(new Label("Errore caricamento: " + e.getMessage()));
        }
    }

    private void openChat(BookedLessonBean lesson) {
        try {
            ChatGraphicController.closeActiveSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            Parent root = loader.load();
            ChatGraphicController controller = loader.getController();

            Stage stage = new Stage();
            ChatGraphicController.registerStage(stage, controller);

            controller.setupLesson(
                    lesson.getTransactionId(),
                    lesson.getPartnerId(),
                    lesson.getDuration(),
                    true,
                    lesson.getSubject(),
                    lesson.getTutorName()
            );

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}