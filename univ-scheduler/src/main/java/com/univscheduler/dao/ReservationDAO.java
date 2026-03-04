package com.univscheduler.dao;

import com.univscheduler.database.DatabaseManager;
import com.univscheduler.models.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private final Connection conn;
    private final RoomDAO roomDAO;
    private final UserDAO userDAO;

    public ReservationDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
        this.roomDAO = new RoomDAO();
        this.userDAO = new UserDAO();
    }

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM reservations ORDER BY date DESC, heure_debut");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> findByDemandeur(int userId) {
        List<Reservation> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM reservations WHERE demandeur_id = ? ORDER BY date DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean hasConflict(int roomId, String date, String heureDebut, String heureFin, int excludeId) {
        try {
            String sql = """
                SELECT COUNT(*) FROM reservations
                WHERE room_id = ? AND date = ? AND id != ? AND statut != 'ANNULEE'
                AND NOT (heure_fin <= ? OR heure_debut >= ?)
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            ps.setString(2, date);
            ps.setInt(3, excludeId);
            ps.setString(4, heureDebut);
            ps.setString(5, heureFin);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean save(Reservation r) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reservations (room_id, demandeur_id, motif, date, heure_debut, heure_fin, statut) VALUES (?,?,?,?,?,?,?)");
            ps.setInt(1, r.getSalle().getId());
            ps.setInt(2, r.getDemandeur().getId());
            ps.setString(3, r.getMotif());
            ps.setString(4, r.getDate().toString());
            ps.setString(5, r.getHeureDebut().toString());
            ps.setString(6, r.getHeureFin().toString());
            ps.setString(7, r.getStatut().name());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateStatut(int id, Reservation.Statut statut) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE reservations SET statut = ? WHERE id = ?");
            ps.setString(1, statut.name());
            ps.setInt(2, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM reservations WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int count() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM reservations");
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setMotif(rs.getString("motif"));
        try { r.setDate(LocalDate.parse(rs.getString("date"))); } catch (Exception e) {}
        try { r.setHeureDebut(LocalTime.parse(rs.getString("heure_debut"))); } catch (Exception e) {}
        try { r.setHeureFin(LocalTime.parse(rs.getString("heure_fin"))); } catch (Exception e) {}
        try { r.setStatut(Reservation.Statut.valueOf(rs.getString("statut"))); } catch (Exception e) { r.setStatut(Reservation.Statut.EN_ATTENTE); }

        int roomId = rs.getInt("room_id");
        if (roomId > 0) r.setSalle(roomDAO.findById(roomId));

        int userId = rs.getInt("demandeur_id");
        if (userId > 0) r.setDemandeur(userDAO.findById(userId));

        return r;
    }
}
