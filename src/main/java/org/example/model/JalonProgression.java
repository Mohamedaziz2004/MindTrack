package org.example.model;

import java.time.LocalDate;

public class JalonProgression {

    private int idJalon;
    private int idObj;
    private String titre;
    private LocalDate dateCible;
    private boolean atteint;
    private LocalDate dateAtteinte;
    private int pourcentageProgression;

    // Constructor vide
    public JalonProgression() {
    }

    // Constructor complet
    public JalonProgression(int idJalon, int idObj, String titre, LocalDate dateCible, 
                           boolean atteint, LocalDate dateAtteinte, int pourcentageProgression) {
        this.idJalon = idJalon;
        this.idObj = idObj;
        this.titre = titre;
        this.dateCible = dateCible;
        this.atteint = atteint;
        this.dateAtteinte = dateAtteinte;
        this.pourcentageProgression = pourcentageProgression;
    }

    // Constructor simple (without id)
    public JalonProgression(String titre, LocalDate dateCible) {
        this.titre = titre;
        this.dateCible = dateCible;
        this.atteint = false;
        this.pourcentageProgression = 0;
    }

    // Getters & Setters
    public int getIdJalon() {
        return idJalon;
    }

    public void setIdJalon(int idJalon) {
        this.idJalon = idJalon;
    }

    public int getIdObj() {
        return idObj;
    }

    public void setIdObj(int idObj) {
        this.idObj = idObj;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public LocalDate getDateCible() {
        return dateCible;
    }

    public void setDateCible(LocalDate dateCible) {
        this.dateCible = dateCible;
    }

    public boolean isAtteint() {
        return atteint;
    }

    public void setAtteint(boolean atteint) {
        this.atteint = atteint;
    }

    public LocalDate getDateAtteinte() {
        return dateAtteinte;
    }

    public void setDateAtteinte(LocalDate dateAtteinte) {
        this.dateAtteinte = dateAtteinte;
    }

    public int getPourcentageProgression() {
        return pourcentageProgression;
    }

    public void setPourcentageProgression(int pourcentageProgression) {
        this.pourcentageProgression = pourcentageProgression;
    }

    @Override
    public String toString() {
        return titre + (atteint ? " ✓" : "");
    }
}
