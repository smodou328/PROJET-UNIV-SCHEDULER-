package com.univscheduler.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Course {
    private int id;
    private String matiere;
    private User enseignant;
    private String classe;
    private String groupe;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private int dureeMinutes;
    private Room salle;
    private String jour;   // LUNDI, MARDI, etc. pour emploi du temps récurrent
    private boolean recurrent;
    private String couleur;
    private String notes;

    public Course() {}

    public Course(int id, String matiere, User enseignant, String classe,
                  String groupe, String jour, LocalTime heureDebut, int dureeMinutes, Room salle) {
        this.id = id;
        this.matiere = matiere;
        this.enseignant = enseignant;
        this.classe = classe;
        this.groupe = groupe;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.dureeMinutes = dureeMinutes;
        this.heureFin = heureDebut.plusMinutes(dureeMinutes);
        this.salle = salle;
        this.recurrent = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }

    public User getEnseignant() { return enseignant; }
    public void setEnseignant(User enseignant) { this.enseignant = enseignant; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
        if (dureeMinutes > 0) this.heureFin = heureDebut.plusMinutes(dureeMinutes);
    }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
        if (heureDebut != null) this.heureFin = heureDebut.plusMinutes(dureeMinutes);
    }

    public Room getSalle() { return salle; }
    public void setSalle(Room salle) { this.salle = salle; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public boolean isRecurrent() { return recurrent; }
    public void setRecurrent(boolean recurrent) { this.recurrent = recurrent; }

    public String getCouleur() { return couleur != null ? couleur : "#3B82F6"; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getHeureDebutStr() {
        return heureDebut != null ? heureDebut.toString() : "";
    }

    public String getHeureFinStr() {
        return heureFin != null ? heureFin.toString() : "";
    }

    @Override
    public String toString() {
        return matiere + " — " + classe + (groupe != null && !groupe.isEmpty() ? " Gr." + groupe : "");
    }
}
