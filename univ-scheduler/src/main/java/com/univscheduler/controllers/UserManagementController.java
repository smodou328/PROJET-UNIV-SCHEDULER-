package com.univscheduler.controllers;

import com.univscheduler.dao.UserDAO;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.net.URL;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colActif;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private Label totalLabel;
    @FXML private Label userNameLabel;

    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Only admin can access
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtils.showError("Accès refusé", "Vous n'avez pas les droits nécessaires.");
            SceneManager.getInstance().showDashboard();
            return;
        }

        userNameLabel.setText(SessionManager.getInstance().getCurrentUser().getNomComplet());
        setupTable();
        setupFilters();
        loadUsers();
    }

    private void setupTable() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().getLabel()));
        colActif.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActif() ? "✅ Actif" : "❌ Inactif"));

        userTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) setStyle("");
                else if (!u.isActif()) setStyle("-fx-background-color: #fee2e2;");
                else setStyle("");
            }
        });

        userTable.setItems(userList);
        userTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && userTable.getSelectionModel().getSelectedItem() != null)
                showEditDialog(userTable.getSelectionModel().getSelectedItem());
        });
    }

    private void setupFilters() {
        roleFilter.getItems().add("Tous les rôles");
        for (User.Role r : User.Role.values()) roleFilter.getItems().add(r.getLabel());
        roleFilter.setValue("Tous les rôles");
        roleFilter.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void loadUsers() {
        userList.setAll(userDAO.findAll());
        totalLabel.setText("Total: " + userList.size() + " utilisateurs");
    }

    private void applyFilter() {
        String search = searchField.getText().toLowerCase();
        String role = roleFilter.getValue();
        userList.setAll(userDAO.findAll().stream().filter(u -> {
            boolean matchSearch = search.isEmpty() ||
                u.getNom().toLowerCase().contains(search) ||
                u.getPrenom().toLowerCase().contains(search) ||
                u.getEmail().toLowerCase().contains(search);
            boolean matchRole = role.equals("Tous les rôles") || u.getRole().getLabel().equals(role);
            return matchSearch && matchRole;
        }).toList());
    }

    @FXML private void showAddDialog() { showEditDialog(null); }

    @FXML
    private void showEditSelectedDialog() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez un utilisateur."); return; }
        showEditDialog(selected);
    }

    private void showEditDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter un Utilisateur" : "Modifier l'Utilisateur");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12); form.setPadding(new Insets(20));
        form.setPrefWidth(420);

        TextField nomField = new TextField(existing != null ? existing.getNom() : "");
        TextField prenomField = new TextField(existing != null ? existing.getPrenom() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        PasswordField pwdField = new PasswordField();
        pwdField.setPromptText(existing != null ? "Laisser vide pour ne pas changer" : "Mot de passe");
        ComboBox<User.Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(User.Role.values());
        if (existing != null) roleCombo.setValue(existing.getRole()); else roleCombo.setValue(User.Role.ETUDIANT);
        CheckBox actifCheck = new CheckBox("Compte actif");
        actifCheck.setSelected(existing == null || existing.isActif());

        form.add(new Label("Nom:"), 0, 0); form.add(nomField, 1, 0);
        form.add(new Label("Prénom:"), 0, 1); form.add(prenomField, 1, 1);
        form.add(new Label("Email:"), 0, 2); form.add(emailField, 1, 2);
        form.add(new Label("Mot de passe:"), 0, 3); form.add(pwdField, 1, 3);
        form.add(new Label("Rôle:"), 0, 4); form.add(roleCombo, 1, 4);
        form.add(actifCheck, 1, 5);

        pane.setContent(form);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                User u = existing != null ? existing : new User();
                u.setNom(nomField.getText().trim());
                u.setPrenom(prenomField.getText().trim());
                u.setEmail(emailField.getText().trim());
                if (!pwdField.getText().isEmpty()) u.setPassword(pwdField.getText());
                else if (existing == null) u.setPassword("password123");
                u.setRole(roleCombo.getValue());
                u.setActif(actifCheck.isSelected());
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (user.getNom().isEmpty() || user.getEmail().isEmpty()) {
                AlertUtils.showError("Erreur", "Nom et email sont obligatoires.");
                return;
            }
            boolean ok = existing == null ? userDAO.save(user) : userDAO.update(user);
            if (ok) { loadUsers(); AlertUtils.showSuccess("Succès", existing == null ? "Utilisateur créé." : "Utilisateur modifié."); }
        });
    }

    @FXML
    private void deleteSelected() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtils.showWarning("Avertissement", "Sélectionnez un utilisateur."); return; }
        if (selected.getId() == SessionManager.getInstance().getCurrentUser().getId()) {
            AlertUtils.showError("Erreur", "Vous ne pouvez pas supprimer votre propre compte."); return;
        }
        if (AlertUtils.showConfirmation("Désactiver", "Désactiver l'utilisateur " + selected.getNomComplet() + " ?")) {
            if (userDAO.delete(selected.getId())) { loadUsers(); AlertUtils.showSuccess("Succès", "Utilisateur désactivé."); }
        }
    }

    @FXML private void goBack() { SceneManager.getInstance().showDashboard(); }
}
