package com.univscheduler.controllers;

import com.univscheduler.dao.ReservationDAO;
import com.univscheduler.dao.RoomDAO;
import com.univscheduler.models.Reservation;
import com.univscheduler.models.Room;
import com.univscheduler.models.User;
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
import javafx.scene.layout.GridPane;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, String> colSalle;
    @FXML private TableColumn<Reservation, String> colDemandeur;
    @FXML private TableColumn<Reservation, String> colMotif;
    @FXML private TableColumn<Reservation, String> colHeures;
    @FXML private TableColumn<Reservation, String> colStatut;
    @FXML private Label totalLabel;
    @FXML private Label userNameLabel;
    @FXML private Button confirmBtn;
    @FXML private Button cancelBtn;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getInstance().getCurrentUser();
        userNameLabel.setText(user.getNomComplet());

        boolean isAdmin = SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isGestionnaire();
        confirmBtn.setVisible(isAdmin);

        setupTable();
        loadReservations();
    }

    private void setupTable() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDate() != null ? data.getValue().getDate().toString() : ""));
        colSalle.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getSalle() != null ? data.getValue().getSalle().getNomComplet() : ""));
        colDemandeur.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDemandeur() != null ? data.getValue().getDemandeur().getNomComplet() : ""));
        colMotif.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMotif()));
        colHeures.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getHeureDebut() + " – " + data.getValue().getHeureFin()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getStatut() != null ? data.getValue().getStatut().getLabel() : ""));

        // Color by status
        reservationTable.setRowFactory(tv -> new TableRow<Reservation>() {
            @Override
            protected void updateItem(Reservation r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setStyle(""); return; }
                switch (r.getStatut()) {
                    case CONFIRMEE -> setStyle("-fx-background-color: #d1fae5;");
                    case ANNULEE -> setStyle("-fx-background-color: #fee2e2;");
                    case EN_ATTENTE -> setStyle("-fx-background-color: #fef3c7;");
                    default -> setStyle("");
                }
            }
        });

        reservationTable.setItems(reservationList);
    }

    private void loadReservations() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (SessionManager.getInstance().canManageSchedule()) {
            reservationList.setAll(reservationDAO.findAll());
        } else {
            reservationList.setAll(reservationDAO.findByDemandeur(user.getId()));
        }
        totalLabel.setText("Total: " + reservationList.size() + " réservations");
    }

    @FXML
    private void showAddDialog() {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Réservation");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12); form.setPadding(new Insets(20));
        form.setPrefWidth(420);

        ComboBox<Room> salleCombo = new ComboBox<>();
        salleCombo.getItems().addAll(roomDAO.findAll());
        salleCombo.setPromptText("Sélectionner une salle");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField heureDebutField = new TextField("08:00");
        TextField heureFinField = new TextField("10:00");
        TextArea motifArea = new TextArea();
        motifArea.setPromptText("Motif de la réservation...");
        motifArea.setPrefRowCount(3);

        form.add(new Label("Salle:"), 0, 0); form.add(salleCombo, 1, 0);
        form.add(new Label("Date:"), 0, 1); form.add(datePicker, 1, 1);
        form.add(new Label("Heure début:"), 0, 2); form.add(heureDebutField, 1, 2);
        form.add(new Label("Heure fin:"), 0, 3); form.add(heureFinField, 1, 3);
        form.add(new Label("Motif:"), 0, 4); form.add(motifArea, 1, 4);

        pane.setContent(form);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Reservation r = new Reservation();
                r.setSalle(salleCombo.getValue());
                r.setDemandeur(SessionManager.getInstance().getCurrentUser());
                r.setDate(datePicker.getValue());
                r.setMotif(motifArea.getText());
                try { r.setHeureDebut(LocalTime.parse(heureDebutField.getText())); } catch (Exception e) {}
                try { r.setHeureFin(LocalTime.parse(heureFinField.getText())); } catch (Exception e) {}
                r.setStatut(Reservation.Statut.EN_ATTENTE);
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(r -> {
            if (r.getSalle() == null || r.getDate() == null) {
                AlertUtils.showError("Erreur", "Salle et date sont obligatoires."); return;
            }
            if (reservationDAO.hasConflict(r.getSalle().getId(), r.getDate().toString(),
                    r.getHeureDebut().toString(), r.getHeureFin().toString(), 0)) {
                AlertUtils.showWarning("Conflit", "⚠ Cette salle est déjà réservée à ce créneau !");
                return;
            }
            if (reservationDAO.save(r)) { loadReservations(); AlertUtils.showSuccess("Succès", "Réservation créée."); }
        });
    }

    @FXML
    private void confirmSelected() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez une réservation."); return; }
        if (reservationDAO.updateStatut(selected.getId(), Reservation.Statut.CONFIRMEE)) {
            loadReservations(); AlertUtils.showSuccess("Succès", "Réservation confirmée.");
        }
    }

    @FXML
    private void cancelSelected() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez une réservation."); return; }
        if (AlertUtils.showConfirmation("Annuler", "Annuler cette réservation ?")) {
            if (reservationDAO.updateStatut(selected.getId(), Reservation.Statut.ANNULEE)) {
                loadReservations(); AlertUtils.showSuccess("Succès", "Réservation annulée.");
            }
        }
    }

    @FXML private void goBack() { SceneManager.getInstance().showDashboard(); }
}
