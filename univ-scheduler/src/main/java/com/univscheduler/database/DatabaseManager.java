package com.univscheduler.database;

import java.sql.*;
import java.io.File;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_NAME = "univ_scheduler.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            createTables();
            insertDefaultData();
            System.out.println("[DB] Base de données initialisée avec succès.");
        } catch (Exception e) {
            System.err.println("[DB] Erreur d'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Users
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                prenom TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                actif INTEGER DEFAULT 1
            )
        """);

        // Buildings
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS buildings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                localisation TEXT,
                nb_etages INTEGER DEFAULT 1,
                description TEXT
            )
        """);

        // Rooms
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS rooms (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT NOT NULL,
                capacite INTEGER NOT NULL,
                type TEXT NOT NULL,
                etage INTEGER DEFAULT 0,
                building_id INTEGER,
                disponible INTEGER DEFAULT 1,
                FOREIGN KEY (building_id) REFERENCES buildings(id)
            )
        """);

        // Equipment
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS equipements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                type TEXT NOT NULL,
                description TEXT,
                room_id INTEGER,
                FOREIGN KEY (room_id) REFERENCES rooms(id)
            )
        """);

        // Courses
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS courses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                matiere TEXT NOT NULL,
                enseignant_id INTEGER,
                classe TEXT NOT NULL,
                groupe TEXT,
                jour TEXT,
                heure_debut TEXT,
                heure_fin TEXT,
                duree_minutes INTEGER DEFAULT 90,
                room_id INTEGER,
                recurrent INTEGER DEFAULT 1,
                couleur TEXT DEFAULT '#3B82F6',
                notes TEXT,
                FOREIGN KEY (enseignant_id) REFERENCES users(id),
                FOREIGN KEY (room_id) REFERENCES rooms(id)
            )
        """);

        // Reservations
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS reservations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                room_id INTEGER,
                demandeur_id INTEGER,
                motif TEXT,
                date TEXT NOT NULL,
                heure_debut TEXT NOT NULL,
                heure_fin TEXT NOT NULL,
                statut TEXT DEFAULT 'EN_ATTENTE',
                FOREIGN KEY (room_id) REFERENCES rooms(id),
                FOREIGN KEY (demandeur_id) REFERENCES users(id)
            )
        """);

        stmt.close();
        System.out.println("[DB] Tables créées.");
    }

    private void insertDefaultData() throws SQLException {
        var rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM users");
        if (rs.getInt(1) > 0) return;

        // Hash dynamique — plus de hash hardcodé
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(
                "admin123",
                org.mindrot.jbcrypt.BCrypt.gensalt()
        );

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (nom, prenom, email, password, role, actif) VALUES (?,?,?,?,?,?)");

        Object[][] users = {
                {"Admin", "Système", "admin@univ.edu", hash, "ADMINISTRATEUR", 1},
                {"Diop", "Mamadou", "gestionnaire@univ.edu", hash, "GESTIONNAIRE", 1},
                {"Ndiaye", "Fatou", "enseignant@univ.edu", hash, "ENSEIGNANT", 1},
                {"Fall", "Ibrahima", "etudiant@univ.edu", hash, "ETUDIANT", 1}
        };

        for (Object[] u : users) {
            ps.setString(1, (String) u[0]);
            ps.setString(2, (String) u[1]);
            ps.setString(3, (String) u[2]);
            ps.setString(4, (String) u[3]);
            ps.setString(5, (String) u[4]);
            ps.setInt(6, (int) u[5]);
            ps.executeUpdate();
        }
        // ... gardez le reste (buildings, rooms, etc.) inchangé
    }
}
