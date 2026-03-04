package com.univscheduler.controllers;

import com.univscheduler.dao.BuildingDAO;
import com.univscheduler.dao.RoomDAO;
import com.univscheduler.models.Building;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RoomManagementController implements Initializable {

    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> colNumero;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, Integer> colCapacite;
    @FXML private TableColumn<Room, String> colBatiment;
    @FXML private TableColumn<Room, String> colDisponible;
    @FXML private TableColumn<Room, Integer> colEtage;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Label totalLabel;
    @FXML private Label userNameLabel;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BuildingDAO buildingDAO = new BuildingDAO();
    private ObservableList<Room> roomList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userNameLabel.setText(SessionManager.getInstance().getCurrentUser().getNomComplet());

        setupTable();
        setupFilters();
        loadRooms();

        boolean canEdit = SessionManager.getInstance().canManageRooms();
        addBtn.setVisible(canEdit);
        editBtn.setVisible(canEdit);
        deleteBtn.setVisible(canEdit);
    }

    private void setupTable() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType() != null ? data.getValue().getType().getLabel() : ""));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colEtage.setCellValueFactory(new PropertyValueFactory<>("etage"));
        colBatiment.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getBatiment() != null ? data.getValue().getBatiment().getNom() : "—"));
        colDisponible.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().isDisponible() ? "✅ Disponible" : "❌ Indisponible"));

        // Color rows based on availability
        roomTable.setRowFactory(tv -> new TableRow<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) setStyle("");
                else if (!room.isDisponible()) setStyle("-fx-background-color: #fee2e2;");
                else setStyle("");
            }
        });

        roomTable.setItems(roomList);
        roomTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && roomTable.getSelectionModel().getSelectedItem() != null) {
                showEditDialog(roomTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupFilters() {
        typeFilter.getItems().add("Tous les types");
        for (Room.TypeSalle t : Room.TypeSalle.values()) typeFilter.getItems().add(t.getLabel());
        typeFilter.setValue("Tous les types");
        typeFilter.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void loadRooms() {
        List<Room> rooms = roomDAO.findAll();
        roomList.setAll(rooms);
        totalLabel.setText("Total: " + rooms.size() + " salles");
    }

    private void applyFilter() {
        String search = searchField.getText().toLowerCase();
        String selectedType = typeFilter.getValue();

        List<Room> all = roomDAO.findAll();
        List<Room> filtered = all.stream().filter(r -> {
            boolean matchSearch = search.isEmpty() ||
                r.getNumero().toLowerCase().contains(search) ||
                (r.getBatiment() != null && r.getBatiment().getNom().toLowerCase().contains(search));
            boolean matchType = selectedType.equals("Tous les types") ||
                (r.getType() != null && r.getType().getLabel().equals(selectedType));
            return matchSearch && matchType;
        }).toList();

        roomList.setAll(filtered);
        totalLabel.setText("Affichage: " + filtered.size() + " / " + all.size() + " salles");
    }

    @FXML
    private void showAddDialog() { showEditDialog(null); }

    @FXML
    private void showEditSelectedDialog() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez une salle."); return; }
        showEditDialog(selected);
    }

    private void showEditDialog(Room existing) {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter une Salle" : "Modifier la Salle");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12); form.setPadding(new Insets(20));

        TextField numeroField = new TextField(existing != null ? existing.getNumero() : "");
        TextField capaciteField = new TextField(existing != null ? String.valueOf(existing.getCapacite()) : "");
        TextField etageField = new TextField(existing != null ? String.valueOf(existing.getEtage()) : "0");
        ComboBox<Room.TypeSalle> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Room.TypeSalle.values());
        if (existing != null) typeCombo.setValue(existing.getType()); else typeCombo.setValue(Room.TypeSalle.TD);
        ComboBox<Building> batimentCombo = new ComboBox<>();
        batimentCombo.getItems().addAll(buildingDAO.findAll());
        if (existing != null) batimentCombo.setValue(existing.getBatiment());
        CheckBox disponibleCheck = new CheckBox("Disponible");
        disponibleCheck.setSelected(existing == null || existing.isDisponible());

        form.add(new Label("Numéro:"), 0, 0); form.add(numeroField, 1, 0);
        form.add(new Label("Capacité:"), 0, 1); form.add(capaciteField, 1, 1);
        form.add(new Label("Étage:"), 0, 2); form.add(etageField, 1, 2);
        form.add(new Label("Type:"), 0, 3); form.add(typeCombo, 1, 3);
        form.add(new Label("Bâtiment:"), 0, 4); form.add(batimentCombo, 1, 4);
        form.add(disponibleCheck, 1, 5);

        pane.setContent(form);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Room r = existing != null ? existing : new Room();
                r.setNumero(numeroField.getText());
                try { r.setCapacite(Integer.parseInt(capaciteField.getText())); } catch (Exception e) { r.setCapacite(30); }
                try { r.setEtage(Integer.parseInt(etageField.getText())); } catch (Exception e) { r.setEtage(0); }
                r.setType(typeCombo.getValue());
                r.setBatiment(batimentCombo.getValue());
                r.setDisponible(disponibleCheck.isSelected());
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(room -> {
            if (room.getNumero().isEmpty()) { AlertUtils.showError("Erreur", "Le numéro est obligatoire."); return; }
            boolean ok = existing == null ? roomDAO.save(room) : roomDAO.update(room);
            if (ok) { loadRooms(); AlertUtils.showSuccess("Succès", existing == null ? "Salle ajoutée." : "Salle modifiée."); }
        });
    }

    @FXML
    private void deleteSelected() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez une salle."); return; }
        if (AlertUtils.showConfirmation("Supprimer", "Supprimer la salle " + selected.getNumero() + " ?")) {
            if (roomDAO.delete(selected.getId())) { loadRooms(); AlertUtils.showSuccess("Succès", "Salle supprimée."); }
        }
    }

    @FXML private void goBack() { SceneManager.getInstance().showDashboard(); }
}
