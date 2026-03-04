package com.univscheduler.dao;

import com.univscheduler.database.DatabaseManager;
import com.univscheduler.models.Building;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingDAO {
    private final Connection conn;

    public BuildingDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public List<Building> findAll() {
        List<Building> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM buildings ORDER BY nom");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Building findById(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM buildings WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean save(Building b) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO buildings (nom, localisation, nb_etages, description) VALUES (?,?,?,?)");
            ps.setString(1, b.getNom());
            ps.setString(2, b.getLocalisation());
            ps.setInt(3, b.getNbEtages());
            ps.setString(4, b.getDescription());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Building b) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE buildings SET nom=?, localisation=?, nb_etages=?, description=? WHERE id=?");
            ps.setString(1, b.getNom());
            ps.setString(2, b.getLocalisation());
            ps.setInt(3, b.getNbEtages());
            ps.setString(4, b.getDescription());
            ps.setInt(5, b.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM buildings WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int count() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM buildings");
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private Building mapRow(ResultSet rs) throws SQLException {
        Building b = new Building();
        b.setId(rs.getInt("id"));
        b.setNom(rs.getString("nom"));
        b.setLocalisation(rs.getString("localisation"));
        b.setNbEtages(rs.getInt("nb_etages"));
        b.setDescription(rs.getString("description"));
        return b;
    }
}
