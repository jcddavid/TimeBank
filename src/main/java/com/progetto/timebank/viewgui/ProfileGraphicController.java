package com.progetto.timebank.viewgui;

import com.progetto.timebank.bean.UserBean;
import com.progetto.timebank.bean.UserSession;
import com.progetto.timebank.controller.ProfileController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ProfileGraphicController {

    @FXML private Label lblUsername;
    @FXML private Label lblEmail;
    @FXML private Label lblBalance;
    @FXML private HBox boxRating;
    @FXML private Label lblRatingNum;
    @FXML private FlowPane paneSkills;

    private final ProfileController controller = new ProfileController();

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            UserBean user = session.getUser();

            lblUsername.setText(user.getUsername());
            lblEmail.setText(user.getEmail());
            lblBalance.setText(user.getBalance() + " ore");

            loadRating(user.getId());
            loadSkillsBadges(user.getSkills());
        }
    }

    private void loadRating(int userId) {
        double rating = controller.getUserRating(userId);

        lblRatingNum.setText(String.format("(%.1f / 5)", rating));
        boxRating.getChildren().clear();

        for (int i = 1; i <= 5; i++) {
            Text star = new Text("★");
            star.setFont(Font.font("System", 24));
            if (i <= Math.round(rating)) {
                star.setFill(Color.GOLD);
            } else {
                star.setFill(Color.LIGHTGRAY);
            }
            boxRating.getChildren().add(star);
        }
    }

    private void loadSkillsBadges(String skills) {
        paneSkills.getChildren().clear();
        if (skills == null || skills.trim().isEmpty()) {
            Label placeholder = new Label("Nessuna competenza inserita.");
            placeholder.setTextFill(Color.GRAY);
            paneSkills.getChildren().add(placeholder);
            return;
        }

        String[] skillArray = skills.split(",");
        for (String s : skillArray) {
            String skillName = s.trim();
            if (!skillName.isEmpty()) {
                Label badge = new Label(skillName);
                badge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15; -fx-font-weight: bold;");
                paneSkills.getChildren().add(badge);
            }
        }
    }
}