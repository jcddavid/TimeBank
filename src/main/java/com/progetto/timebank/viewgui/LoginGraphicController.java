package com.progetto.timebank.viewgui;

import com.progetto.timebank.controller.LoginController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginGraphicController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private final LoginController loginController = new LoginController();

    @FXML
    protected void onLoginButtonClick() {
        String user = txtUsername.getText();
        String pwd = txtPassword.getText();

        if (user.isEmpty() || pwd.isEmpty()) {
            showError("Inserisci username e password.");
            return;
        }

        try {
            boolean success = loginController.login(user, pwd);

            if (success) {
                goToDashboard();
            } else {
                showError("Username o Password errati.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore di connessione al Database.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore di Accesso");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goToDashboard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TimeBank - Dashboard");

            stage.setOnCloseRequest(event -> loginController.logout());
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossibile caricare la Dashboard.");
        }
    }

    @FXML
    protected void onRegisterLinkClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 500);
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}