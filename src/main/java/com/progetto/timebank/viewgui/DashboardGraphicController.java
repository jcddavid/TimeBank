package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.DashboardController;
import com.progetto.timebank.controller.LoginController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class DashboardGraphicController {
    @FXML private BorderPane mainPane;
    @FXML private Label lblWelcome;
    @FXML private Label lblBalance;

    private final LoginController loginController = new LoginController();
    private final DashboardController dashboardController = new DashboardController();

    @FXML
    public void initialize() {
        refreshDashboard();
        loadCenter("/Home.fxml");
    }

    // Metodo chiamato anche quando passi col mouse sopra la dashboard
    @FXML
    public void refreshDashboard() {
        dashboardController.refreshSessionData();

        UserSession session = UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            UserBean user = session.getUser();
            if (lblWelcome != null) lblWelcome.setText("Benvenuto, " + user.getUsername());
            if (lblBalance != null) lblBalance.setText("Saldo: " + user.getBalance() + " ore");
        }
    }

    @FXML
    protected void onLogoutClick() {
        loginController.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadCenter(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            mainPane.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossibile caricare la vista: " + fxmlPath);
        }
    }

    @FXML protected void onHomeClick() { loadCenter("/Home.fxml"); }
    @FXML protected void onSearchClick() { loadCenter("/SearchLesson.fxml"); }
    @FXML protected void onBookLessonClick() { loadCenter("/BookedLessons.fxml"); }
    @FXML protected void onRequestsClick() { loadCenter("/IncomingRequests.fxml"); }
    @FXML protected void onMyOffersClick() { loadCenter("/MyOffers.fxml"); }
}
