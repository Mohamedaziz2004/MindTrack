package org.example.model;

import java.time.LocalDateTime;

public class PlanificateurIntelligent {

    private int idPlanificateur;
    private int idObj;
    private String modeOrganisation; // "priorite", "progression", "temps"
    private int capaciteQuotidienne; // nombre d'actions par jour
    private LocalDateTime derniereGeneration;

    // Constructor vide
    public PlanificateurIntelligent() {
    }

    // Constructor complet
    public PlanificateurIntelligent(int idPlanificateur, int idObj, String modeOrganisation, 
                                   int capaciteQuotidienne, LocalDateTime derniereGeneration) {
        this.idPlanificateur = idPlanificateur;
        this.idObj = idObj;
        this.modeOrganisation = modeOrganisation;
        this.capaciteQuotidienne = capaciteQuotidienne;
        this.derniereGeneration = derniereGeneration;
    }

    // Constructor simple
    public PlanificateurIntelligent(int idObj, String modeOrganisation, int capaciteQuotidienne) {
        this.idObj = idObj;
        this.modeOrganisation = modeOrganisation;
        this.capaciteQuotidienne = capaciteQuotidienne;
    }

    // Getters & Setters
    public int getIdPlanificateur() {
        return idPlanificateur;
    }

    public void setIdPlanificateur(int idPlanificateur) {
        this.idPlanificateur = idPlanificateur;
    }

    public int getIdObj() {
        return idObj;
    }

    public void setIdObj(int idObj) {
        this.idObj = idObj;
    }

    public String getModeOrganisation() {
        return modeOrganisation;
    }

    public void setModeOrganisation(String modeOrganisation) {
        this.modeOrganisation = modeOrganisation;
    }

    public int getCapaciteQuotidienne() {
        return capaciteQuotidienne;
    }

    public void setCapaciteQuotidienne(int capaciteQuotidienne) {
        this.capaciteQuotidienne = capaciteQuotidienne;
    }

    public LocalDateTime getDerniereGeneration() {
        return derniereGeneration;
    }

    public void setDerniereGeneration(LocalDateTime derniereGeneration) {
        this.derniereGeneration = derniereGeneration;
    }

    @Override
    public String toString() {
        return "Planificateur [Mode: " + modeOrganisation + ", Capacité: " + capaciteQuotidienne + "/jour]";
    }
}
