package com.univscheduler;

import com.univscheduler.database.DatabaseManager;
import com.univscheduler.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database
        DatabaseManager.getInstance().initialize();

        // Setup scene manager
        SceneManager.getInstance().setPrimaryStage(primaryStage);

        // Load login screen
        SceneManager.getInstance().showLogin();

        primaryStage.setTitle("UNIV-SCHEDULER — Gestion des Salles et Emplois du Temps");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
