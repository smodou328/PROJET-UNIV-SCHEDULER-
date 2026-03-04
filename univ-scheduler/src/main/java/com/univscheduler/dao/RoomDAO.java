package com.univscheduler.dao;

import com.univscheduler.database.DatabaseManager;
import com.univscheduler.models.Building;
import com.univscheduler.models.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    private final Connection conn;
    private final BuildingDAO buildingDAO;

    public RoomDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
        this.buildingDAO = new BuildingDAO();
    }

    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT r.*, b.nom as b_nom FROM rooms r LEFT JOIN buildings b ON r.building_id = b.id ORDER BY r.numero");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Room findById(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM rooms WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Room> findAvailable(String jour, String heureDebut, String heureFin) {
        List<Room> list = new ArrayList<>();
        try {
            String sql = """
                SELECT r.* FROM rooms r
                WHERE r.disponible = 1
                AND r.id NOT IN (
                    SELECT DISTINCT c.room_id FROM courses c
                    WHERE c.jour = ?
                    AND NOT (c.heure_fin <= ? OR c.heure_debut >= ?)
                    AND c.room_id IS NOT NULL
                )
                ORDER BY r.numero
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, jour);
            ps.setString(2, heureDebut);
            ps.setString(3, heureFin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Room> findByType(Room.TypeSalle type) {
        List<Room> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM rooms WHERE type = ?");
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Room> findByCapaciteMin(int capaciteMin) {
        List<Room> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM rooms WHERE capacite >= ?");
            ps.setInt(1, capaciteMin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean save(Room r) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO rooms (numero, capacite, type, etage, building_id, disponible) VALUES (?,?,?,?,?,?)");
            ps.setString(1, r.getNumero());
            ps.setInt(2, r.getCapacite());
            ps.setString(3, r.getType().name());
            ps.setInt(4, r.getEtage());
            ps.setInt(5, r.getBatiment() != null ? r.getBatiment().getId() : 0);
            ps.setInt(6, r.isDisponible() ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Room r) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE rooms SET numero=?, capacite=?, type=?, etage=?, building_id=?, disponible=? WHERE id=?");
            ps.setString(1, r.getNumero());
            ps.setInt(2, r.getCapacite());
            ps.setString(3, r.getType().name());
            ps.setInt(4, r.getEtage());
            ps.setInt(5, r.getBatiment() != null ? r.getBatiment().getId() : 0);
            ps.setInt(6, r.isDisponible() ? 1 : 0);
            ps.setInt(7, r.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM rooms WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int count() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM rooms");
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int countDisponibles() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM rooms WHERE disponible = 1");
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt("id"));
        r.setNumero(rs.getString("numero"));
        r.setCapacite(rs.getInt("capacite"));
        try { r.setType(Room.TypeSalle.valueOf(rs.getString("type"))); } catch (Exception e) { r.setType(Room.TypeSalle.TD); }
        r.setEtage(rs.getInt("etage"));
        r.setDisponible(rs.getInt("disponible") == 1);
        int buildingId = rs.getInt("building_id");
        if (buildingId > 0) r.setBatiment(buildingDAO.findById(buildingId));
        return r;
    }
}
