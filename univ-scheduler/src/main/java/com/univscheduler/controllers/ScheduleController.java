package com.univscheduler.controllers;

import com.univscheduler.dao.CourseDAO;
import com.univscheduler.dao.RoomDAO;
import com.univscheduler.dao.UserDAO;
import com.univscheduler.models.Course;
import com.univscheduler.models.Room;
import com.univscheduler.models.User;
import com.univscheduler.utils.AlertUtils;
import com.univscheduler.utils.SceneManager;
import com.univscheduler.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class ScheduleController implements Initializable {

    @FXML private GridPane scheduleGrid;
    @FXML private ComboBox<String> classeFilter;
    @FXML private ComboBox<User> enseignantFilter;
    @FXML private Label titleLabel;
    @FXML private VBox courseSidebar;
    @FXML private Label userNameLabel;
    @FXML private Button addCourseBtn;

    private final String[] JOURS = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"};
    private final String[] JOURS_LABELS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private final int HEURE_DEBUT = 7;
    private final int HEURE_FIN = 20;

    private final CourseDAO courseDAO = new CourseDAO();
    private final UserDAO userDAO = new UserDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getInstance().getCurrentUser();
        userNameLabel.setText(user.getNomComplet());

        boolean canEdit = SessionManager.getInstance().canManageSchedule();
        addCourseBtn.setVisible(canEdit);
        addCourseBtn.setManaged(canEdit);

        // Load classes for filter
        classeFilter.getItems().add("Toutes les classes");
        classeFilter.getItems().addAll(courseDAO.findAllClasses());
        classeFilter.setValue("Toutes les classes");
        classeFilter.setOnAction(e -> refreshSchedule());

        // Load teachers
        enseignantFilter.getItems().add(null);
        enseignantFilter.getItems().addAll(userDAO.findByRole(User.Role.ENSEIGNANT));
        enseignantFilter.setOnAction(e -> refreshSchedule());

        buildGrid();
        refreshSchedule();
    }

    private void buildGrid() {
        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear();
        scheduleGrid.getRowConstraints().clear();

        // Column widths: time column + 6 day columns
        ColumnConstraints timeCol = new ColumnConstraints(60);
        scheduleGrid.getColumnConstraints().add(timeCol);
        for (int i = 0; i < JOURS.length; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(120);
            scheduleGrid.getColumnConstraints().add(col);
        }

        // Header row
        RowConstraints headerRow = new RowConstraints(40);
        scheduleGrid.getRowConstraints().add(headerRow);

        // Empty top-left
        Label emptyHeader = new Label("");
        emptyHeader.setStyle("-fx-background-color: #1e293b;");
        GridPane.setConstraints(emptyHeader, 0, 0);
        scheduleGrid.getChildren().add(emptyHeader);

        // Day headers
        for (int d = 0; d < JOURS_LABELS.length; d++) {
            Label dayLabel = new Label(JOURS_LABELS[d]);
            dayLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; " +
                              "-fx-background-color: #1e293b; -fx-alignment: center; -fx-padding: 10;");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setMaxHeight(Double.MAX_VALUE);
            GridPane.setConstraints(dayLabel, d + 1, 0);
            scheduleGrid.getChildren().add(dayLabel);
        }

        // Time rows (every 30 min)
        int row = 1;
        for (int h = HEURE_DEBUT; h < HEURE_FIN; h++) {
            for (int m = 0; m < 60; m += 30) {
                RowConstraints rc = new RowConstraints(30);
                scheduleGrid.getRowConstraints().add(rc);

                // Time label every hour
                if (m == 0) {
                    Label timeLabel = new Label(String.format("%02d:00", h));
                    timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px; -fx-padding: 0 4 0 0; -fx-alignment: center-right;");
                    timeLabel.setMaxWidth(Double.MAX_VALUE);
                    GridPane.setConstraints(timeLabel, 0, row);
                    scheduleGrid.getChildren().add(timeLabel);
                }

                // Grid cells
                for (int d = 0; d < JOURS.length; d++) {
                    Pane cell = new Pane();
                    String borderStyle = "-fx-border-color: #e5e7eb; -fx-border-width: 0 1 " + (m == 0 ? "1" : "0") + " 0;";
                    cell.setStyle(borderStyle + "-fx-background-color: white;");
                    cell.setPrefHeight(30);
                    GridPane.setConstraints(cell, d + 1, row);
                    scheduleGrid.getChildren().add(cell);
                }

                row++;
            }
        }
    }

    private void refreshSchedule() {
        // Remove existing course cards (those with style containing course-card)
        scheduleGrid.getChildren().removeIf(n -> n instanceof VBox);

        String selectedClasse = classeFilter.getValue();
        User selectedEnseignant = enseignantFilter.getValue();

        List<Course> courses;
        if (selectedEnseignant != null) {
            courses = courseDAO.findByEnseignant(selectedEnseignant.getId());
        } else if (selectedClasse != null && !selectedClasse.equals("Toutes les classes")) {
            courses = courseDAO.findByClasse(selectedClasse);
        } else {
            courses = courseDAO.findAll();
        }

        for (Course course : courses) {
            if (course.getJour() == null || course.getHeureDebut() == null) continue;

            int dayIndex = getDayIndex(course.getJour());
            if (dayIndex < 0) continue;

            double startRow = getRowForTime(course.getHeureDebut());
            double endRow = getRowForTime(course.getHeureFin() != null ? course.getHeureFin() : course.getHeureDebut().plusMinutes(90));
            int rowSpan = Math.max(1, (int)(endRow - startRow));

            VBox courseCard = createCourseBlock(course);
            GridPane.setConstraints(courseCard, dayIndex + 1, (int) startRow, 1, rowSpan);
            GridPane.setMargin(courseCard, new Insets(1));
            scheduleGrid.getChildren().add(courseCard);
        }
    }

    private VBox createCourseBlock(Course course) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(4, 6, 4, 6));
        String color = course.getCouleur();
        card.setStyle("-fx-background-color: " + color + "e0; " +
                      "-fx-background-radius: 6; -fx-cursor: hand;");
        card.setMaxWidth(Double.MAX_VALUE);

        Label matiereLabel = new Label(course.getMatiere());
        matiereLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: white; -fx-wrap-text: true;");
        matiereLabel.setWrapText(true);

        String salleText = course.getSalle() != null ? course.getSalle().getNumero() : "?";
        Label salleLabel = new Label("📍" + salleText);
        salleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ffffffc0;");

        card.getChildren().addAll(matiereLabel, salleLabel);

        Tooltip tip = new Tooltip(
            course.getMatiere() + "\n" +
            course.getHeureDebutStr() + " – " + course.getHeureFinStr() + "\n" +
            "Classe: " + course.getClasse() + (course.getGroupe() != null && !course.getGroupe().isEmpty() ? " Gr." + course.getGroupe() : "") + "\n" +
            (course.getEnseignant() != null ? "Prof: " + course.getEnseignant().getNomComplet() : "") + "\n" +
            (course.getSalle() != null ? "Salle: " + course.getSalle().getNomComplet() : "")
        );
        Tooltip.install(card, tip);

        if (SessionManager.getInstance().canManageSchedule()) {
            card.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) showEditCourseDialog(course);
            });
        }

        return card;
    }

    private int getDayIndex(String jour) {
        for (int i = 0; i < JOURS.length; i++) {
            if (JOURS[i].equalsIgnoreCase(jour)) return i;
        }
        return -1;
    }

    private double getRowForTime(LocalTime time) {
        double minutesFromStart = (time.getHour() - HEURE_DEBUT) * 60.0 + time.getMinute();
        return 1 + (minutesFromStart / 30.0);
    }

    @FXML
    private void showAddCourseDialog() {
        showEditCourseDialog(null);
    }

    private void showEditCourseDialog(Course existing) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouveau Cours" : "Modifier le Cours");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        TextField matiereField = new TextField(existing != null ? existing.getMatiere() : "");
        TextField classeField = new TextField(existing != null ? existing.getClasse() : "");
        TextField groupeField = new TextField(existing != null ? existing.getGroupe() : "");
        ComboBox<String> jourCombo = new ComboBox<>();
        jourCombo.getItems().addAll("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI");
        if (existing != null) jourCombo.setValue(existing.getJour());
        TextField heureDebutField = new TextField(existing != null ? existing.getHeureDebutStr() : "08:00");
        TextField dureeField = new TextField(existing != null ? String.valueOf(existing.getDureeMinutes()) : "90");
        ComboBox<User> enseignantCombo = new ComboBox<>();
        enseignantCombo.getItems().addAll(userDAO.findByRole(User.Role.ENSEIGNANT));
        if (existing != null) enseignantCombo.setValue(existing.getEnseignant());
        ComboBox<Room> salleCombo = new ComboBox<>();
        salleCombo.getItems().addAll(roomDAO.findAll());
        if (existing != null) salleCombo.setValue(existing.getSalle());
        ColorPicker colorPicker = new ColorPicker();
        try { colorPicker.setValue(Color.web(existing != null ? existing.getCouleur() : "#3B82F6")); } catch (Exception e) {}

        form.add(new Label("Matière:"), 0, 0); form.add(matiereField, 1, 0);
        form.add(new Label("Classe:"), 0, 1); form.add(classeField, 1, 1);
        form.add(new Label("Groupe:"), 0, 2); form.add(groupeField, 1, 2);
        form.add(new Label("Jour:"), 0, 3); form.add(jourCombo, 1, 3);
        form.add(new Label("Heure début:"), 0, 4); form.add(heureDebutField, 1, 4);
        form.add(new Label("Durée (min):"), 0, 5); form.add(dureeField, 1, 5);
        form.add(new Label("Enseignant:"), 0, 6); form.add(enseignantCombo, 1, 6);
        form.add(new Label("Salle:"), 0, 7); form.add(salleCombo, 1, 7);
        form.add(new Label("Couleur:"), 0, 8); form.add(colorPicker, 1, 8);

        pane.setContent(form);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Course c = existing != null ? existing : new Course();
                c.setMatiere(matiereField.getText());
                c.setClasse(classeField.getText());
                c.setGroupe(groupeField.getText());
                c.setJour(jourCombo.getValue());
                try { c.setHeureDebut(LocalTime.parse(heureDebutField.getText())); } catch (Exception e) {}
                try { c.setDureeMinutes(Integer.parseInt(dureeField.getText())); } catch (Exception e) { c.setDureeMinutes(90); }
                c.setEnseignant(enseignantCombo.getValue());
                c.setSalle(salleCombo.getValue());
                Color col = colorPicker.getValue();
                c.setCouleur(String.format("#%02X%02X%02X", (int)(col.getRed()*255), (int)(col.getGreen()*255), (int)(col.getBlue()*255)));
                c.setRecurrent(true);
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            if (course.getMatiere().isEmpty() || course.getJour() == null) {
                AlertUtils.showError("Erreur", "Matière et jour sont obligatoires.");
                return;
            }
            if (courseDAO.hasConflict(course)) {
                AlertUtils.showWarning("Conflit détecté", "⚠ Cette salle est déjà occupée à ce créneau !");
            }
            boolean ok = existing == null ? courseDAO.save(course) : courseDAO.update(course);
            if (ok) {
                refreshSchedule();
                AlertUtils.showSuccess("Succès", existing == null ? "Cours ajouté avec succès." : "Cours modifié.");
            }
        });
    }

    @FXML private void goBack() { SceneManager.getInstance().showDashboard(); }
}
