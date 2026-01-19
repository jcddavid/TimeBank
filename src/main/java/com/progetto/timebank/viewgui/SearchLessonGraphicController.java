package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.BookingController;
import com.progetto.timebank.controller.SearchLessonController;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.InsufficientBalanceException;
import com.progetto.timebank.exception.PersistenceException;
import com.progetto.timebank.model.Course;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


import java.util.List;

public class SearchLessonGraphicController {
    @FXML private ComboBox<Course> cmbSubject;
    @FXML private TableView<UserBean> tableUsers;
    @FXML private TableColumn<UserBean, String> colUser;
    @FXML private TableColumn<UserBean, String> colSkills;
    @FXML private TableColumn<UserBean, String> colRating;
    @FXML private TableColumn<UserBean, UserBean> colAction;

    @FXML
    public void initialize() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colSkills.setCellValueFactory(new PropertyValueFactory<>("skills"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("formattedRating"));
        cmbSubject.setItems(FXCollections.observableArrayList(Course.values()));

        colAction.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Prenota");

            @Override
            protected void updateItem(UserBean person, boolean empty) {
                btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                super.updateItem(person, empty);
                if (person == null) { setGraphic(null); return; }
                setGraphic(btn);
                btn.setOnAction(event -> handleBooking(person));
            }
        });
    }

    @FXML
    protected void onSearchClick() {
        Course selected = cmbSubject.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Seleziona una materia.");
            return;
        }

        SearchLessonController controller = new SearchLessonController();
        int myId = UserSession.getInstance().getUser().getId();

        try {
            List<UserBean> results = controller.searchTutorsBySubject(selected.toString(), myId);
            tableUsers.setItems(FXCollections.observableArrayList(results));
            if (results.isEmpty()) showAlert(Alert.AlertType.INFORMATION, "Info", "Nessun tutor trovato.");
        } catch (PersistenceException e) {
            showAlert(Alert.AlertType.ERROR, "Errore Sistema", e.getMessage());
        }
    }

    private void handleBooking(UserBean tutor) {
        BookingController controller = new BookingController();
        String subject = cmbSubject.getValue() != null ? cmbSubject.getValue().toString() : "Generica";
        int myId = UserSession.getInstance().getUser().getId();

        try {
            controller.bookLesson(myId, tutor.getId(), subject);
            showAlert(Alert.AlertType.INFORMATION, "Successo", "Richiesta inviata a " + tutor.getUsername());
        } catch (InsufficientBalanceException e) {
            showAlert(Alert.AlertType.WARNING, "Saldo Insufficiente", e.getMessage());
        } catch (DuplicateBookingException e) {
            showAlert(Alert.AlertType.INFORMATION, "Già Prenotato", e.getMessage());
        } catch (PersistenceException e) {
            showAlert(Alert.AlertType.ERROR, "Errore Sistema", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}