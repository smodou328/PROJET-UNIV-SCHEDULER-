package com.univscheduler.utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    public void setPrimaryStage(Stage stage) { this.primaryStage = stage; }
    public Stage getPrimaryStage() { return primaryStage; }

    public void showLogin() {
        loadScene("/fxml/Login.fxml", "Connexion — UNIV-SCHEDULER", 500, 600);
        primaryStage.setResizable(false);
    }

    public void showDashboard() {
        loadScene("/fxml/Dashboard.fxml", "UNIV-SCHEDULER — Tableau de Bord", 1200, 750);
        primaryStage.setResizable(true);
    }

    public void showSchedule() {
        loadScene("/fxml/ScheduleView.fxml", "UNIV-SCHEDULER — Emploi du Temps", 1200, 750);
    }

    public void showRoomManagement() {
        loadScene("/fxml/RoomManagement.fxml", "UNIV-SCHEDULER — Gestion des Salles", 1200, 750);
    }

    public void showBuildingManagement() {
        loadScene("/fxml/BuildingManagement.fxml", "UNIV-SCHEDULER — Gestion des Bâtiments", 1000, 700);
    }

    public void showUserManagement() {
        loadScene("/fxml/UserManagement.fxml", "UNIV-SCHEDULER — Gestion des Utilisateurs", 1100, 700);
    }

    public void showReservation() {
        loadScene("/fxml/ReservationView.fxml", "UNIV-SCHEDULER — Réservations", 1100, 700);
    }

    public void showSearch() {
        loadScene("/fxml/SearchView.fxml", "UNIV-SCHEDULER — Recherche de Salles", 1000, 650);
    }

    public void showReports() {
        loadScene("/fxml/ReportsView.fxml", "UNIV-SCHEDULER — Rapports & Statistiques", 1200, 750);
    }

    private void loadScene(String fxmlPath, String title, double width, double height) {
        try {
            URL fxmlUrl = SceneManager.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                String path = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                fxmlUrl = SceneManager.class.getClassLoader().getResource(path);
            }
            if (fxmlUrl == null) {
                System.err.println("[SceneManager] FXML introuvable: " + fxmlPath);
                showErrorScene(title, "Fichier introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);

            URL cssUrl = SceneManager.class.getResource("/css/styles.css");
            if (cssUrl == null)
                cssUrl = SceneManager.class.getClassLoader().getResource("css/styles.css");
            if (cssUrl != null)
                scene.getStylesheets().add(cssUrl.toExternalForm());

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("[SceneManager] Erreur: " + fxmlPath + " => " + e.getMessage());
            e.printStackTrace();
            showErrorScene(title, e.getMessage());
        }
    }

    private void showErrorScene(String title, String details) {
        VBox vbox = new VBox(16);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 40;");
        Label icon = new Label("⚠");
        icon.setStyle("-fx-font-size: 48px;");
        Label label = new Label("Erreur de chargement de l'interface");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        Label detail = new Label(details != null ? details : "Fichier FXML introuvable");
        detail.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        detail.setWrapText(true);
        vbox.getChildren().addAll(icon, label, detail);
        primaryStage.setTitle("Erreur — " + title);
        primaryStage.setScene(new Scene(vbox, 600, 300));
    }

    public <T> T loadFXML(String fxmlPath) throws IOException {
        URL url = SceneManager.class.getResource(fxmlPath);
        if (url == null)
            url = SceneManager.class.getClassLoader().getResource(
                fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath);
        FXMLLoader loader = new FXMLLoader(url);
        return loader.load();
    }
}
