package com.univscheduler.controllers;

import com.univscheduler.dao.BuildingDAO;
import com.univscheduler.dao.RoomDAO;
import com.univscheduler.models.Building;
import com.univscheduler.utils.AlertUtils;
import com.univscheduler.utils.SceneManager;
import com.univscheduler.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.net.URL;
import java.util.ResourceBundle;

public class BuildingManagementController implements Initializable {

    @FXML private TableView<Building> buildingTable;
    @FXML private TableColumn<Building, String> colNom;
    @FXML private TableColumn<Building, String> colLocalisation;
    @FXML private TableColumn<Building, Integer> colEtages;
    @FXML private TableColumn<Building, String> colDescription;
    @FXML private Label totalLabel;
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;

    private final BuildingDAO buildingDAO = new BuildingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private ObservableList<Building> buildingList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userNameLabel.setText(SessionManager.getInstance().getCurrentUser().getNomComplet());
        setupTable();
        loadBuildings();
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void setupTable() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colEtages.setCellValueFactory(new PropertyValueFactory<>("nbEtages"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        buildingTable.setItems(buildingList);

        buildingTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && buildingTable.getSelectionModel().getSelectedItem() != null) {
                showEditDialog(buildingTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void loadBuildings() {
        buildingList.setAll(buildingDAO.findAll());
        totalLabel.setText("Total: " + buildingList.size() + " bâtiments");
    }

    private void applyFilter() {
        String search = searchField.getText().toLowerCase();
        buildingList.setAll(buildingDAO.findAll().stream().filter(b ->
            search.isEmpty() ||
            b.getNom().toLowerCase().contains(search) ||
            (b.getLocalisation() != null && b.getLocalisation().toLowerCase().contains(search))
        ).toList());
    }

    @FXML private void showAddDialog() { showEditDialog(null); }

    @FXML
    private void showEditSelectedDialog() {
        Building selected = buildingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez un bâtiment."); return; }
        showEditDialog(selected);
    }

    private void showEditDialog(Building existing) {
        Dialog<Building> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter un Bâtiment" : "Modifier le Bâtiment");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12); form.setPadding(new Insets(20));
        form.setPrefWidth(400);

        TextField nomField = new TextField(existing != null ? existing.getNom() : "");
        nomField.setPromptText("Ex: Bâtiment A");
        TextField locField = new TextField(existing != null ? existing.getLocalisation() : "");
        locField.setPromptText("Ex: Campus Principal, Aile Nord");
        Spinner<Integer> etagesSpinner = new Spinner<>(1, 20, existing != null ? existing.getNbEtages() : 1);
        TextArea descArea = new TextArea(existing != null ? existing.getDescription() : "");
        descArea.setPromptText("Description du bâtiment...");
        descArea.setPrefRowCount(3);

        form.add(new Label("Nom:"), 0, 0); form.add(nomField, 1, 0);
        form.add(new Label("Localisation:"), 0, 1); form.add(locField, 1, 1);
        form.add(new Label("Nb d'étages:"), 0, 2); form.add(etagesSpinner, 1, 2);
        form.add(new Label("Description:"), 0, 3); form.add(descArea, 1, 3);

        pane.setContent(form);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Building b = existing != null ? existing : new Building();
                b.setNom(nomField.getText().trim());
                b.setLocalisation(locField.getText().trim());
                b.setNbEtages(etagesSpinner.getValue());
                b.setDescription(descArea.getText().trim());
                return b;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(b -> {
            if (b.getNom().isEmpty()) { AlertUtils.showError("Erreur", "Le nom est obligatoire."); return; }
            boolean ok = existing == null ? buildingDAO.save(b) : buildingDAO.update(b);
            if (ok) { loadBuildings(); AlertUtils.showSuccess("Succès", existing == null ? "Bâtiment ajouté." : "Bâtiment modifié."); }
        });
    }

    @FXML
    private void deleteSelected() {
        Building selected = buildingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez un bâtiment."); return; }
        if (AlertUtils.showConfirmation("Supprimer", "Supprimer le bâtiment " + selected.getNom() + " ?")) {
            if (buildingDAO.delete(selected.getId())) { loadBuildings(); AlertUtils.showSuccess("Succès", "Bâtiment supprimé."); }
        }
    }

    @FXML private void goBack() { SceneManager.getInstance().showDashboard(); }
}
