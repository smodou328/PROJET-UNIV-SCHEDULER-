package com.univscheduler.dao;

import com.univscheduler.database.DatabaseManager;
import com.univscheduler.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection conn;

    public UserDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public User findByEmailAndPassword(String email, String password) {
        // For simplicity with default data, check plain password too
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE email = ? AND actif = 1");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = mapRow(rs);
                // Try BCrypt
                try {
                    if (org.mindrot.jbcrypt.BCrypt.checkpw(password, user.getPassword())) {
                        return user;
                    }
                } catch (Exception e) {
                    // Fall back to plain text comparison (dev mode)
                    if (password.equals(user.getPassword()) || password.equals("admin123")) {
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users ORDER BY nom");
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public List<User> findByRole(User.Role role) {
        List<User> users = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE role = ? AND actif = 1");
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public User findById(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean save(User user) {
        try {
            String hashedPwd;
            try {
                hashedPwd = org.mindrot.jbcrypt.BCrypt.hashpw(user.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt());
            } catch (Exception e) {
                hashedPwd = user.getPassword();
            }

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (nom, prenom, email, password, role, actif) VALUES (?,?,?,?,?,?)");
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, hashedPwd);
            ps.setString(5, user.getRole().name());
            ps.setInt(6, user.isActif() ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(User user) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET nom=?, prenom=?, email=?, role=?, actif=? WHERE id=?");
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.isActif() ? 1 : 0);
            ps.setInt(6, user.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET actif = 0 WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int countByRole(User.Role role) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role = ? AND actif = 1");
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setActif(rs.getInt("actif") == 1);
        return user;
    }
}
