package com.univscheduler;

/**
 * Launcher séparé — Contourne l'erreur "JavaFX runtime components are missing"
 * Cette classe est le vrai point d'entrée. Elle appelle MainApp.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
