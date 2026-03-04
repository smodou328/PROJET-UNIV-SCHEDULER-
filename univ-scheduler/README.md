# 🎓 UNIV-SCHEDULER
## Application de Gestion Intelligente des Salles et des Emplois du Temps

---

## 📋 Description

UNIV-SCHEDULER est une application JavaFX complète pour la gestion des salles et emplois du temps universitaires.

---

## 🚀 Installation & Lancement

### Prérequis
- **Java 17+** (JDK 17 minimum)
- **Maven 3.8+**
- **IntelliJ IDEA** (recommandé)

### Étapes dans IntelliJ IDEA

1. **Ouvrir le projet**
   - `File → Open` → Sélectionner le dossier `univ-scheduler`
   - IntelliJ détectera automatiquement le `pom.xml` Maven

2. **Attendre le téléchargement des dépendances**
   - Maven va automatiquement télécharger JavaFX, SQLite, etc.

3. **Configurer le Run/Debug**
   - `Run → Edit Configurations → + → Application`
   - **Main class:** `com.univscheduler.MainApp`
   - **VM Options** (si Java 17+):
     ```
     --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
     ```
   - OU utiliser: `mvn javafx:run`

4. **Lancer l'application**
   - Clic droit sur `MainApp.java → Run`

---

## 🔑 Comptes de Démonstration

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Administrateur | admin@univ.edu | admin123 |
| Gestionnaire | gestionnaire@univ.edu | admin123 |
| Enseignant | enseignant@univ.edu | admin123 |
| Étudiant | etudiant@univ.edu | admin123 |

---

## 📁 Structure du Projet

```
univ-scheduler/
├── pom.xml                          # Configuration Maven
├── univ_scheduler.db                # Base SQLite (auto-créée)
└── src/
    └── main/
        ├── java/com/univscheduler/
        │   ├── MainApp.java          # Point d'entrée
        │   ├── models/               # Entités (User, Room, Building, Course, Reservation)
        │   ├── database/             # Gestionnaire SQLite
        │   ├── dao/                  # Couche d'accès aux données
        │   ├── controllers/          # Contrôleurs JavaFX FXML
        │   └── utils/                # Utilitaires (Session, Scène, Alertes)
        └── resources/
            ├── fxml/                 # Fichiers d'interface FXML
            │   ├── Login.fxml
            │   ├── Dashboard.fxml
            │   ├── ScheduleView.fxml
            │   ├── RoomManagement.fxml
            │   ├── BuildingManagement.fxml
            │   ├── UserManagement.fxml
            │   ├── ReservationView.fxml
            │   ├── SearchView.fxml
            │   └── ReportsView.fxml
            └── css/
                └── styles.css        # Thème visuel global
```

---

## 🖥️ Interfaces Disponibles

| Interface | Description |
|-----------|-------------|
| **Login** | Page de connexion avec accès demo |
| **Dashboard** | Tableau de bord avec statistiques et cours du jour |
| **Emploi du Temps** | Grille hebdomadaire interactive |
| **Gestion des Salles** | CRUD complet des salles |
| **Gestion des Bâtiments** | CRUD complet des bâtiments |
| **Gestion des Utilisateurs** | Admin uniquement - CRUD utilisateurs |
| **Réservations** | Système de réservation ponctuelle |
| **Recherche** | Recherche de salles disponibles par critères |
| **Rapports** | Graphiques et statistiques d'utilisation |

---

## 🛠️ Technologies Utilisées

- **Java 17** — Langage principal
- **JavaFX 17** — Interface graphique (FXML + CSS)
- **SQLite** via `sqlite-jdbc` — Base de données locale
- **Maven** — Gestion des dépendances
- **iText 5** — Export PDF
- **Apache POI** — Export Excel
- **BCrypt** — Hachage des mots de passe

---

## 🗃️ Base de Données

La base SQLite `univ_scheduler.db` est créée automatiquement au premier lancement dans le répertoire de travail.

### Tables
- `users` — Utilisateurs
- `buildings` — Bâtiments
- `rooms` — Salles
- `equipements` — Équipements des salles
- `courses` — Cours (emploi du temps)
- `reservations` — Réservations ponctuelles

---

## ⚠️ Dépannage

**Erreur JavaFX non trouvé :**
```xml
<!-- Vérifier que le pom.xml contient bien : -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.6</version>
</dependency>
```

**Erreur de module Java :**
Ajouter dans VM options :
```
--add-opens java.base/java.lang=ALL-UNNAMED
```

---

## 👨‍💻 Équipe

Projet Licence 2 Informatique — POO Java  
Présentation: **16 Mars à 08h00**
