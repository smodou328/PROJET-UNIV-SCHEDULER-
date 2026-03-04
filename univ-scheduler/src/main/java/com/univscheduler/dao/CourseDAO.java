package com.univscheduler.dao;

import com.univscheduler.database.DatabaseManager;
import com.univscheduler.models.Course;
import com.univscheduler.models.Room;
import com.univscheduler.models.User;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    private final Connection conn;
    private final UserDAO userDAO;
    private final RoomDAO roomDAO;

    public CourseDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
        this.userDAO = new UserDAO();
        this.roomDAO = new RoomDAO();
    }

    public List<Course> findAll() {
        List<Course> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM courses ORDER BY jour, heure_debut");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Course> findByJour(String jour) {
        List<Course> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM courses WHERE jour = ? ORDER BY heure_debut");
            ps.setString(1, jour);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Course> findByClasse(String classe) {
        List<Course> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM courses WHERE classe = ? ORDER BY jour, heure_debut");
            ps.setString(1, classe);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Course> findByEnseignant(int enseignantId) {
        List<Course> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM courses WHERE enseignant_id = ? ORDER BY jour, heure_debut");
            ps.setInt(1, enseignantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String> findAllClasses() {
        List<String> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT DISTINCT classe FROM courses ORDER BY classe");
            while (rs.next()) list.add(rs.getString("classe"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean hasConflict(Course course) {
        try {
            String sql = """
                SELECT COUNT(*) FROM courses
                WHERE jour = ? AND id != ?
                AND room_id = ?
                AND NOT (heure_fin <= ? OR heure_debut >= ?)
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, course.getJour());
            ps.setInt(2, course.getId());
            ps.setInt(3, course.getSalle() != null ? course.getSalle().getId() : -1);
            ps.setString(4, course.getHeureDebutStr());
            ps.setString(5, course.getHeureFinStr());
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean hasTeacherConflict(Course course) {
        try {
            String sql = """
                SELECT COUNT(*) FROM courses
                WHERE jour = ? AND id != ?
                AND enseignant_id = ?
                AND NOT (heure_fin <= ? OR heure_debut >= ?)
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, course.getJour());
            ps.setInt(2, course.getId());
            ps.setInt(3, course.getEnseignant() != null ? course.getEnseignant().getId() : -1);
            ps.setString(4, course.getHeureDebutStr());
            ps.setString(5, course.getHeureFinStr());
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean save(Course c) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO courses (matiere, enseignant_id, classe, groupe, jour, heure_debut, heure_fin, duree_minutes, room_id, recurrent, couleur, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, c.getMatiere());
            ps.setInt(2, c.getEnseignant() != null ? c.getEnseignant().getId() : 0);
            ps.setString(3, c.getClasse());
            ps.setString(4, c.getGroupe());
            ps.setString(5, c.getJour());
            ps.setString(6, c.getHeureDebutStr());
            ps.setString(7, c.getHeureFinStr());
            ps.setInt(8, c.getDureeMinutes());
            ps.setInt(9, c.getSalle() != null ? c.getSalle().getId() : 0);
            ps.setInt(10, c.isRecurrent() ? 1 : 0);
            ps.setString(11, c.getCouleur());
            ps.setString(12, c.getNotes());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Course c) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE courses SET matiere=?, enseignant_id=?, classe=?, groupe=?, jour=?, heure_debut=?, heure_fin=?, duree_minutes=?, room_id=?, couleur=?, notes=? WHERE id=?");
            ps.setString(1, c.getMatiere());
            ps.setInt(2, c.getEnseignant() != null ? c.getEnseignant().getId() : 0);
            ps.setString(3, c.getClasse());
            ps.setString(4, c.getGroupe());
            ps.setString(5, c.getJour());
            ps.setString(6, c.getHeureDebutStr());
            ps.setString(7, c.getHeureFinStr());
            ps.setInt(8, c.getDureeMinutes());
            ps.setInt(9, c.getSalle() != null ? c.getSalle().getId() : 0);
            ps.setString(10, c.getCouleur());
            ps.setString(11, c.getNotes());
            ps.setInt(12, c.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int count() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM courses");
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setMatiere(rs.getString("matiere"));
        c.setClasse(rs.getString("classe"));
        c.setGroupe(rs.getString("groupe"));
        c.setJour(rs.getString("jour"));
        try { c.setHeureDebut(LocalTime.parse(rs.getString("heure_debut"))); } catch (Exception e) {}
        try { c.setHeureFin(LocalTime.parse(rs.getString("heure_fin"))); } catch (Exception e) {}
        c.setDureeMinutes(rs.getInt("duree_minutes"));
        c.setRecurrent(rs.getInt("recurrent") == 1);
        c.setCouleur(rs.getString("couleur"));
        c.setNotes(rs.getString("notes"));

        int ensId = rs.getInt("enseignant_id");
        if (ensId > 0) c.setEnseignant(userDAO.findById(ensId));

        int roomId = rs.getInt("room_id");
        if (roomId > 0) c.setSalle(roomDAO.findById(roomId));

        return c;
    }
}
