package com.univscheduler.models;

public class Building {
    private int id;
    private String nom;
    private String localisation;
    private int nbEtages;
    private String description;

    public Building() {}

    public Building(int id, String nom, String localisation, int nbEtages, String description) {
        this.id = id;
        this.nom = nom;
        this.localisation = localisation;
        this.nbEtages = nbEtages;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public int getNbEtages() { return nbEtages; }
    public void setNbEtages(int nbEtages) { this.nbEtages = nbEtages; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return nom + " — " + localisation; }
}
