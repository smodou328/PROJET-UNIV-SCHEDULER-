package com.univscheduler.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private int id;
    private String numero;
    private int capacite;
    private TypeSalle type;
    private int etage;
    private Building batiment;
    private List<Equipment> equipements;
    private boolean disponible;

    public enum TypeSalle {
        TD("Salle TD"),
        TP("Salle TP"),
        AMPHI("Amphithéâtre"),
        REUNION("Salle de Réunion"),
        LABO("Laboratoire"),
        INFORMATIQUE("Salle Informatique");

        private final String label;
        TypeSalle(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    public Room() {
        this.equipements = new ArrayList<>();
        this.disponible = true;
    }

    public Room(int id, String numero, int capacite, TypeSalle type, int etage, Building batiment) {
        this.id = id;
        this.numero = numero;
        this.capacite = capacite;
        this.type = type;
        this.etage = etage;
        this.batiment = batiment;
        this.equipements = new ArrayList<>();
        this.disponible = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public TypeSalle getType() { return type; }
    public void setType(TypeSalle type) { this.type = type; }

    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }

    public Building getBatiment() { return batiment; }
    public void setBatiment(Building batiment) { this.batiment = batiment; }

    public List<Equipment> getEquipements() { return equipements; }
    public void setEquipements(List<Equipment> equipements) { this.equipements = equipements; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getNomComplet() {
        return (batiment != null ? batiment.getNom() + " - " : "") + "Salle " + numero;
    }

    @Override
    public String toString() { return getNomComplet() + " (" + (type != null ? type.getLabel() : "") + ", " + capacite + " places)"; }
}
