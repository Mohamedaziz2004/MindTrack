package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Objectif {

    private int idObj;
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut; // "Non commencée", "En cours", "Complétée"
    private int idU;
    
    // Relations
    private List<JalonProgression> jalons;
    private List<PlanAction> planActions;
    private PlanificateurIntelligent planificateur;

    // Constructor vide
    public Objectif() {
        this.jalons = new ArrayList<>();
        this.planActions = new ArrayList<>();
    }

    // Constructor complet
    public Objectif(int idObj, String titre, String description, LocalDate dateDebut, 
                    LocalDate dateFin, String statut, int idU) {
        this.idObj = idObj;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.idU = idU;
        this.jalons = new ArrayList<>();
        this.planActions = new ArrayList<>();
    }

    // Getters & Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdU() {
        return idU;
    }

    public void setIdU(int idU) {
        this.idU = idU;
    }

    public List<JalonProgression> getJalons() {
        return jalons;
    }

    public void setJalons(List<JalonProgression> jalons) {
        this.jalons = jalons;
    }

    public List<PlanAction> getPlanActions() {
        return planActions;
    }

    public void setPlanActions(List<PlanAction> planActions) {
        this.planActions = planActions;
    }

    public PlanificateurIntelligent getPlanificateur() {
        return planificateur;
    }

    public void setPlanificateur(PlanificateurIntelligent planificateur) {
        this.planificateur = planificateur;
    }

    // Calculate progress percentage based on milestones
    public int calculateProgression() {
        if (jalons.isEmpty()) {
            return 0;
        }
        int completed = (int) jalons.stream().filter(JalonProgression::isAtteint).count();
        return (completed * 100) / jalons.size();
    }

    @Override
    public String toString() {
        return titre + " (" + statut + ")";
    }
}
