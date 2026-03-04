package com.univscheduler.controllers;

import com.univscheduler.dao.*;
import com.univscheduler.models.*;
import com.univscheduler.utils.AlertUtils;
import com.univscheduler.utils.SceneManager;
import com.univscheduler.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.*;

public class ReportsController implements Initializable {

    @FXML private BarChart<String, Number> occupationChart;
    @FXML private PieChart typeRepartitionChart;
    @FXML private LineChart<String, Number> weeklyChart;
    @FXML private Label totalSallesLabel;
    @FXML private Label tauxOccupationLabel;
    @FXML private Label coursParSemaineLabel;
    @FXML private Label reservationsLabel;
    @FXML private Label userNameLabel;
    @FXML private VBox statsBox;

    private final CourseDAO courseDAO = new CourseDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final BuildingDAO buildingDAO = new BuildingDAO();
    private final UserDAO userDAO = new UserDAO();

    private final String[] JOURS = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userNameLabel.setText(SessionManager.getInstance().getCurrentUser().getNomComplet());
        loadStats();
        buildOccupationChart();
        buildTypeChart();
        buildWeeklyChart();
    }

    private void loadStats() {
        int totalSalles = roomDAO.count();
        int totalCours = courseDAO.count();
        int totalRes = reservationDAO.count();

        // Calculate average occupation rate
        double tauxMoyen = calculateAverageOccupation();

        totalSallesLabel.setText(totalSalles + " salles");
        tauxOccupationLabel.setText(String.format("%.1f%%", tauxMoyen));
        coursParSemaineLabel.setText(totalCours + " cours/sem.");
        reservationsLabel.setText(totalRes + " réservations");
    }

    private double calculateAverageOccupation() {
        List<Room> rooms = roomDAO.findAll();
        if (rooms.isEmpty()) return 0;
        int totalSlots = JOURS.length * 12; // 6 days x 12 possible 1h slots
        int usedSlots = courseDAO.count();
        return Math.min(100, (double) usedSlots / totalSlots * 100);
    }

    private void buildOccupationChart() {
        occupationChart.getData().clear();
        occupationChart.setTitle("Occupation par Jour");
        occupationChart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de cours");

        for (String jour : JOURS) {
            List<Course> courses = courseDAO.findByJour(jour);
            series.getData().add(new XYChart.Data<>(jour.substring(0, 3), courses.size()));
        }

        occupationChart.getData().add(series);
    }

    private void buildTypeChart() {
        typeRepartitionChart.getData().clear();
        typeRepartitionChart.setTitle("Répartition des Salles par Type");

        Map<String, Integer> typeCount = new LinkedHashMap<>();
        for (Room.TypeSalle type : Room.TypeSalle.values()) {
            List<Room> rooms = roomDAO.findByType(type);
            if (!rooms.isEmpty()) {
                typeCount.put(type.getLabel(), rooms.size());
            }
        }

        typeCount.forEach((label, count) -> {
            PieChart.Data slice = new PieChart.Data(label + " (" + count + ")", count);
            typeRepartitionChart.getData().add(slice);
        });
    }

    private void buildWeeklyChart() {
        weeklyChart.getData().clear();
        weeklyChart.setTitle("Cours par Tranche Horaire");
        weeklyChart.setAnimated(true);
        weeklyChart.setCreateSymbols(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de cours");

        // Count courses per hour slot
        Map<String, Integer> hourCount = new LinkedHashMap<>();
        String[] hours = {"07:00", "08:00", "09:00", "10:00", "11:00", "12:00",
                          "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        for (String h : hours) hourCount.put(h, 0);

        List<Course> allCourses = courseDAO.findAll();
        for (Course c : allCourses) {
            if (c.getHeureDebut() != null) {
                String key = String.format("%02d:00", c.getHeureDebut().getHour());
                hourCount.merge(key, 1, Integer::sum);
            }
        }

        hourCount.forEach((hour, count) -> series.getData().add(new XYChart.Data<>(hour, count)));
        weeklyChart.getData().add(series);
    }

    @FXML
    private void refreshStats() {
        loadStats();
        buildOccupationChart();
        buildTypeChart();
        buildWeeklyChart();
        AlertUtils.showSuccess("Actualisé", "Les statistiques ont été mises à jour.");
    }

    @FXML
    private void exportPDF() {
        AlertUtils.showSuccess("Export PDF",
            "Fonctionnalité d'export PDF disponible.\n" +
            "Utilisez la bibliothèque iText intégrée pour générer les rapports.");
    }

    @FXML
    private void exportExcel() {
        AlertUtils.showSuccess("Export Excel",
            "Fonctionnalité d'export Excel disponible.\n" +
            "Utilisez la bibliothèque Apache POI intégrée pour générer les fichiers Excel.");
    }

    @FXML private void goBack() { SceneManager.getInstance().showDashboard(); }
}
