package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.MyOffersController; // Usa il Controller
import com.progetto.timebank.exception.PersistenceException;
import com.progetto.timebank.model.Course;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import java.util.ArrayList;
import java.util.List;

public class MyOffersGraphicController {

    @FXML private ComboBox<Course> cmbCourses;
    @FXML private ListView<String> listSkills;

    private final MyOffersController controller = new MyOffersController();

    @FXML
    public void initialize() {
        cmbCourses.setItems(FXCollections.observableArrayList(Course.values()));
        loadSkills();
    }

    private void loadSkills() {
        UserSession session = UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            String skills = session.getUser().getSkills();
            if (skills != null && !skills.isEmpty()) {
                String[] split = skills.split(",");
                List<String> cleanList = new ArrayList<>();
                for (String s : split) {
                    if (!s.trim().isEmpty()) cleanList.add(s.trim());
                }
                listSkills.setItems(FXCollections.observableArrayList(cleanList));
            } else {
                listSkills.getItems().clear();
            }
        }
    }

    @FXML
    protected void onAddClick() {
        Course selected = cmbCourses.getValue();
        if (selected == null) {
            showAlert("Seleziona una materia.");
            return;
        }

        UserSession session = UserSession.getInstance();
        UserBean user = session.getUser();
        String currentSkills = (user.getSkills() == null) ? "" : user.getSkills();

        if (currentSkills.contains(selected.toString())) {
            showAlert("Hai già questa competenza.");
            return;
        }

        String newSkills = currentSkills.isEmpty() ? selected.toString() : currentSkills + ", " + selected.toString();
        updateSkillsThroughController(user, newSkills);
    }

    @FXML
    protected void onRemoveClick() {
        String selectedSkill = listSkills.getSelectionModel().getSelectedItem();
        if (selectedSkill == null) {
            showAlert("Seleziona una competenza dalla lista da rimuovere.");
            return;
        }

        UserSession session = UserSession.getInstance();
        UserBean user = session.getUser();

        List<String> currentList = listSkills.getItems();
        currentList.remove(selectedSkill);

        String newSkillsString = String.join(", ", currentList);
        updateSkillsThroughController(user, newSkillsString);
    }

    private void updateSkillsThroughController(UserBean user, String newSkills) {
        try {
            controller.updateSkills(user.getId(), newSkills);

            user.setSkills(newSkills);
            loadSkills();
            showAlert("Competenze aggiornate con successo!");

        } catch (PersistenceException e) {
            showAlert("Errore: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}