package com.univscheduler.controllers;

import com.univscheduler.dao.UserDAO;
import com.univscheduler.models.User;
import com.univscheduler.utils.AlertUtils;
import com.univscheduler.utils.SceneManager;
import com.univscheduler.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private VBox loginCard;
    @FXML private Label versionLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        versionLabel.setText("v1.0 — UNIV-SCHEDULER 2024");

        // Fade-in animation
        FadeTransition fade = new FadeTransition(Duration.millis(800), loginCard);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        // Enter key triggers login
        passwordField.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());

        // Demo hints
        emailField.setPromptText("admin@univ.edu");
        passwordField.setPromptText("admin123");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir votre email et mot de passe.");
            return;
        }

        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        User user = userDAO.findByEmailAndPassword(email, password);

        loadingIndicator.setVisible(false);
        loginButton.setDisable(false);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            SceneManager.getInstance().showDashboard();
        } else {
            showError("Email ou mot de passe incorrect.");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    private void fillDemo(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        Object ud = btn.getUserData();
        String role = ud != null ? ud.toString() : "";
        switch (role) {
            case "admin" -> { emailField.setText("admin@univ.edu"); passwordField.setText("admin123"); }
            case "gestionnaire" -> { emailField.setText("gestionnaire@univ.edu"); passwordField.setText("admin123"); }
            case "enseignant" -> { emailField.setText("enseignant@univ.edu"); passwordField.setText("admin123"); }
            case "etudiant" -> { emailField.setText("etudiant@univ.edu"); passwordField.setText("admin123"); }
        }
    }



    @FXML
    private void fillAdmin() {
        emailField.setText("admin@univ.edu");
        passwordField.setText("admin123");
    }

    @FXML
    private void fillGestionnaire() {
        emailField.setText("gestionnaire@univ.edu");
        passwordField.setText("admin123"); // ← était "gest123"
    }

    @FXML
    private void fillEnseignant() {
        emailField.setText("enseignant@univ.edu");
        passwordField.setText("admin123"); // ← était "ens123"
    }

    @FXML
    private void fillEtudiant() {
        emailField.setText("etudiant@univ.edu");
        passwordField.setText("admin123");
    }
}
