package com.univscheduler.models;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Role role;
    private boolean actif;

    public enum Role {
        ADMINISTRATEUR("Administrateur"),
        GESTIONNAIRE("Gestionnaire d'emploi du temps"),
        ENSEIGNANT("Enseignant"),
        ETUDIANT("Étudiant");

        private final String label;
        Role(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    public User() {}

    public User(int id, String nom, String prenom, String email, String password, Role role, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.role = role;
        this.actif = actif;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() { return getNomComplet() + " (" + role.getLabel() + ")"; }
}
