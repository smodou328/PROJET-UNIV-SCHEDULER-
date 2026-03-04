package com.univscheduler.controllers;

import com.univscheduler.dao.RoomDAO;
import com.univscheduler.models.Room;
import com.univscheduler.utils.AlertUtils;
import com.univscheduler.utils.SceneManager;
import com.univscheduler.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SearchController implements Initializable {

    @FXML private ComboBox<String> jourCombo;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private ComboBox<String> typeFilter;
    @FXML private TableView<Room> resultsTable;
    @FXML private TableColumn<Room, String> colNumero;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, Integer> colCapacite;
    @FXML private TableColumn<Room, String> colBatiment;
    @FXML private TableColumn<Room, String> colEtage;
    @FXML private Label resultsLabel;
    @FXML private Label userNameLabel;
    @FXML private VBox searchResultsBox;

    private final RoomDAO roomDAO = new RoomDAO();
    private ObservableList<Room> results = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userNameLabel.setText(SessionManager.getInstance().getCurrentUser().getNomComplet());

        jourCombo.getItems().addAll("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI");
        jourCombo.setValue("LUNDI");
        heureDebutField.setText("08:00");
        heureFinField.setText("10:00");

        typeFilter.getItems().add("Tous les types");
        for (Room.TypeSalle t : Room.TypeSalle.values()) typeFilter.getItems().add(t.getLabel());
        typeFilter.setValue("Tous les types");

        setupTable();
        searchNow();
    }

    private void setupTable() {
        colNumero.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNumero()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getType() != null ? data.getValue().getType().getLabel() : ""));
        colCapacite.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCapacite()).asObject());
        colBatiment.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getBatiment() != null ? data.getValue().getBatiment().getNom() : ""));
        colEtage.setCellValueFactory(data -> new SimpleStringProperty("Étage " + data.getValue().getEtage()));

        resultsTable.setItems(results);

        // Double-click to reserve
        resultsTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Room selected = resultsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    AlertUtils.showSuccess("Salle sélectionnée",
                        "Salle: " + selected.getNomComplet() + "\n" +
                        "Capacité: " + selected.getCapacite() + " places\n" +
                        "Type: " + (selected.getType() != null ? selected.getType().getLabel() : "—") + "\n\n" +
                        "Allez dans 'Réservations' pour effectuer une réservation.");
                }
            }
        });
    }

    @FXML
    private void searchNow() {
        String jour = jourCombo.getValue();
        String debut = heureDebutField.getText();
        String fin = heureFinField.getText();
        int capaciteMin = capaciteSpinner != null ? capaciteSpinner.getValue() : 1;
        String type = typeFilter.getValue();

        List<Room> available = roomDAO.findAvailable(jour, debut, fin);

        // Apply additional filters
        List<Room> filtered = available.stream().filter(r -> {
            boolean matchCapacite = r.getCapacite() >= capaciteMin;
            boolean matchType = type.equals("Tous les types") ||
                (r.getType() != null && r.getType().getLabel().equals(type));
            return matchCapacite && matchType;
        }).toList();

        results.setAll(filtered);
        resultsLabel.setText(filtered.size() + " salle(s) disponible(s) trouvée(s)");

        if (filtered.isEmpty()) {
            resultsLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else {
            resultsLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
    }

    @FXML private void goBack() { SceneManager.getInstance().showDashboard(); }
}
