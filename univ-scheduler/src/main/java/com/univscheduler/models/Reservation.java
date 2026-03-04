package com.univscheduler.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private int id;
    private Room salle;
    private User demandeur;
    private String motif;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Statut statut;

    public enum Statut {
        EN_ATTENTE("En attente"),
        CONFIRMEE("Confirmée"),
        ANNULEE("Annulée"),
        TERMINEE("Terminée");

        private final String label;
        Statut(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    public Reservation() { this.statut = Statut.EN_ATTENTE; }

    public Reservation(int id, Room salle, User demandeur, String motif,
                       LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        this.id = id;
        this.salle = salle;
        this.demandeur = demandeur;
        this.motif = motif;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = Statut.EN_ATTENTE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Room getSalle() { return salle; }
    public void setSalle(Room salle) { this.salle = salle; }

    public User getDemandeur() { return demandeur; }
    public void setDemandeur(User demandeur) { this.demandeur = demandeur; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Réservation #" + id + " — " + (salle != null ? salle.getNumero() : "") + " le " + date;
    }
}
