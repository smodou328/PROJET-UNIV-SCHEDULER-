package com.univscheduler.utils;

import com.univscheduler.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public boolean isLoggedIn() { return currentUser != null; }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == User.Role.ADMINISTRATEUR;
    }

    public boolean isGestionnaire() {
        return isLoggedIn() && currentUser.getRole() == User.Role.GESTIONNAIRE;
    }

    public boolean isEnseignant() {
        return isLoggedIn() && currentUser.getRole() == User.Role.ENSEIGNANT;
    }

    public boolean isEtudiant() {
        return isLoggedIn() && currentUser.getRole() == User.Role.ETUDIANT;
    }

    public boolean canManageSchedule() {
        return isAdmin() || isGestionnaire();
    }

    public boolean canManageRooms() {
        return isAdmin() || isGestionnaire();
    }

    public void logout() { currentUser = null; }
}
