package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.controller.RegisterController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterGraphicController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPassword;

    @FXML
    protected void onConfirmRegisterClick() {
        
        UserBean bean = new UserBean();
        bean.setUsername(txtUsername.getText());
        bean.setEmail(txtEmail.getText());
        bean.setPassword(txtPassword.getText());

        
        RegisterController controller = new RegisterController();
        try {
            controller.registerUser(bean);

            
            showAlert(Alert.AlertType.INFORMATION, "Successo", "Registrazione completata! Ora puoi accedere.");
            onBackClick(); 

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Dati non validi", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore DB", "Errore tecnico durante il salvataggio.");
        }
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TimeBank - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}