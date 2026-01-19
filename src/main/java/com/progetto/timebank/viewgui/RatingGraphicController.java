package com.progetto.timebank.viewgui;

import com.progetto.timebank.controller.RatingController; // USA CONTROLLER
import com.progetto.timebank.exception.PersistenceException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class RatingGraphicController {

    @FXML private Button star1;
    @FXML private Button star2;
    @FXML private Button star3;
    @FXML private Button star4;
    @FXML private Button star5;

    private int currentRating = 0;
    private int transactionId;
    private List<Button> stars;

    @FXML
    public void initialize() {
        stars = new ArrayList<>();
        stars.add(star1); stars.add(star2); stars.add(star3); stars.add(star4); stars.add(star5);
    }

    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    @FXML
    protected void onStarClick(ActionEvent event) {
        Button clickedStar = (Button) event.getSource();
        if (clickedStar == star1) currentRating = 1;
        else if (clickedStar == star2) currentRating = 2;
        else if (clickedStar == star3) currentRating = 3;
        else if (clickedStar == star4) currentRating = 4;
        else if (clickedStar == star5) currentRating = 5;
        updateStarsVisual();
    }

    private void updateStarsVisual() {
        for (int i = 0; i < stars.size(); i++) {
            String style = (i < currentRating)
                    ? "-fx-font-size: 30px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-cursor: hand;"
                    : "-fx-font-size: 30px; -fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-cursor: hand;";
            stars.get(i).setStyle(style);
        }
    }

    @FXML
    protected void onSubmitClick() {
        if (currentRating == 0) {
            new Alert(Alert.AlertType.WARNING, "Seleziona almeno una stella!").show();
            return;
        }

        RatingController controller = new RatingController();
        try {
            controller.submitRating(transactionId, currentRating);
            new Alert(Alert.AlertType.INFORMATION, "Grazie per il feedback!").showAndWait();
            ((Stage) star1.getScene().getWindow()).close();
        } catch (PersistenceException e) {
            new Alert(Alert.AlertType.ERROR, "Errore salvataggio: " + e.getMessage()).show();
        }
    }
}