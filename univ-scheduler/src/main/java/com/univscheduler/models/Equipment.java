package com.univscheduler.models;

public class Equipment {
    private int id;
    private String nom;
    private String description;
    private TypeEquipement type;
    private int roomId;

    public enum TypeEquipement {
        VIDEOPROJECTEUR("Vidéoprojecteur"),
        TABLEAU_INTERACTIF("Tableau Interactif"),
        CLIMATISATION("Climatisation"),
        ORDINATEUR("Ordinateur"),
        SONORISATION("Sonorisation"),
        CAMERA("Caméra"),
        WIFI("WiFi"),
        TABLEAU_BLANC("Tableau Blanc");

        private final String label;
        TypeEquipement(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    public Equipment() {}

    public Equipment(int id, String nom, TypeEquipement type, int roomId) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.roomId = roomId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TypeEquipement getType() { return type; }
    public void setType(TypeEquipement type) { this.type = type; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    @Override
    public String toString() { return nom + " (" + (type != null ? type.getLabel() : "") + ")"; }
}
