package com.univscheduler.controllers;

import com.univscheduler.dao.*;
import com.univscheduler.models.Course;
import com.univscheduler.models.User;
import com.univscheduler.utils.SceneManager;
import com.univscheduler.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label totalSallesLabel;
    @FXML private Label sallesDispoLabel;
    @FXML private Label totalCoursLabel;
    @FXML private Label totalBatimentsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private VBox todayScheduleBox;
    @FXML private VBox menuBox;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // Menu items visibility
    @FXML private Button btnRooms;
    @FXML private Button btnBuildings;
    @FXML private Button btnUsers;
    @FXML private Button btnSchedule;
    @FXML private Button btnReservation;
    @FXML private Button btnSearch;
    @FXML private Button btnReports;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BuildingDAO buildingDAO = new BuildingDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = SessionManager.getInstance().getCurrentUser();

        // User info in sidebar
        userNameLabel.setText(user.getNomComplet());
        userRoleLabel.setText(user.getRole().getLabel());

        // Welcome message
        welcomeLabel.setText("Bonjour, " + user.getPrenom() + " 👋");
        roleLabel.setText(user.getRole().getLabel());
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)));

        // Load stats
        loadStats();

        // Today's schedule
        loadTodaySchedule();

        // Configure menu based on role
        configureMenu(user.getRole());
    }

    private void loadStats() {
        totalSallesLabel.setText(String.valueOf(roomDAO.count()));
        sallesDispoLabel.setText(String.valueOf(roomDAO.countDisponibles()));
        totalCoursLabel.setText(String.valueOf(courseDAO.count()));
        totalBatimentsLabel.setText(String.valueOf(buildingDAO.count()));
        totalUsersLabel.setText(String.valueOf(userDAO.findAll().size()));
        totalReservationsLabel.setText(String.valueOf(reservationDAO.count()));
    }

    private void loadTodaySchedule() {
        todayScheduleBox.getChildren().clear();
        String aujourdhui = LocalDate.now()
            .getDayOfWeek()
            .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH)
            .toUpperCase();

        User user = SessionManager.getInstance().getCurrentUser();
        List<Course> courses;

        if (user.getRole() == User.Role.ENSEIGNANT) {
            courses = courseDAO.findByEnseignant(user.getId());
            courses = courses.stream().filter(c -> c.getJour() != null && c.getJour().toUpperCase().equals(aujourdhui)).toList();
        } else {
            courses = courseDAO.findByJour(aujourdhui);
        }

        if (courses.isEmpty()) {
            Label noCourse = new Label("Aucun cours aujourd'hui");
            noCourse.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
            todayScheduleBox.getChildren().add(noCourse);
        } else {
            for (Course c : courses) {
                HBox card = createCourseCard(c);
                todayScheduleBox.getChildren().add(card);
            }
        }
    }

    private HBox createCourseCard(Course c) {
        HBox card = new HBox(12);
        card.setStyle("-fx-background-color: " + c.getCouleur() + "20; " +
                      "-fx-border-color: " + c.getCouleur() + "; " +
                      "-fx-border-radius: 8; -fx-background-radius: 8; " +
                      "-fx-padding: 10; -fx-cursor: hand;");

        Rectangle colorBar = new Rectangle(4, 40);
        try { colorBar.setFill(Color.web(c.getCouleur())); } catch (Exception e) { colorBar.setFill(Color.BLUE); }

        VBox info = new VBox(3);
        Label matiereLabel = new Label(c.getMatiere());
        matiereLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label timeLabel = new Label(c.getHeureDebutStr() + " – " + c.getHeureFinStr());
        timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        Label salleLabel = new Label(c.getSalle() != null ? "📍 " + c.getSalle().getNomComplet() : "Salle non définie");
        salleLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 11px;");
        info.getChildren().addAll(matiereLabel, timeLabel, salleLabel);

        card.getChildren().addAll(colorBar, info);
        return card;
    }

    private void configureMenu(User.Role role) {
        boolean isAdmin = role == User.Role.ADMINISTRATEUR;
        boolean isGest = role == User.Role.GESTIONNAIRE;
        boolean isEns = role == User.Role.ENSEIGNANT;

        btnUsers.setVisible(isAdmin);
        btnUsers.setManaged(isAdmin);
        btnBuildings.setVisible(isAdmin || isGest);
        btnBuildings.setManaged(isAdmin || isGest);
        btnRooms.setVisible(isAdmin || isGest);
        btnRooms.setManaged(isAdmin || isGest);
        btnReports.setVisible(isAdmin || isGest);
        btnReports.setManaged(isAdmin || isGest);
    }

    @FXML private void goToSchedule() { SceneManager.getInstance().showSchedule(); }
    @FXML private void goToRooms() { SceneManager.getInstance().showRoomManagement(); }
    @FXML private void goToBuildings() { SceneManager.getInstance().showBuildingManagement(); }
    @FXML private void goToUsers() { SceneManager.getInstance().showUserManagement(); }
    @FXML private void goToReservation() { SceneManager.getInstance().showReservation(); }
    @FXML private void goToSearch() { SceneManager.getInstance().showSearch(); }
    @FXML private void goToReports() { SceneManager.getInstance().showReports(); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.getInstance().showLogin();
    }
}
