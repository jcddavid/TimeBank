package com.progetto.timebank;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.model.dao.DAOFactory;
import com.progetto.timebank.model.dao.UserDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("TimeBank - Benvenuto");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            System.out.println("[Main] Chiusura dalla X rilevata.");
            forceLogout();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Main] Shutdown Hook attivato (Chiusura forzata).");
            forceLogout();
        }));

        launch();
    }

    private static void forceLogout() {
        UserSession session = UserSession.getInstance();

        if (session != null && session.getUser() != null) {
            UserBean user = session.getUser();
            UserDAO dao = DAOFactory.getUserDAO();
            try {
                System.out.println("[Main] Logout forzato per utente: " + user.getUsername());
                dao.setUserOnline(user.getId(), false);
            } catch (SQLException e) {
                System.err.println("[Main] Errore durante il logout forzato: " + e.getMessage());
            }
        } else {
            System.out.println("[Main] Nessun utente loggato da disconnettere.");
        }
    }
}