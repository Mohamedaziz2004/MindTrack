package org.example.model;

public class PlanAction {

    private int idPlan;
    private int idObj;
    private String etape;
    private int priorite; // 1 = haute, 2 = moyenne, 3 = basse

    // Constructor vide
    public PlanAction() {
    }

    // Constructor complet
    public PlanAction(int idPlan, int idObj, String etape, int priorite) {
        this.idPlan = idPlan;
        this.idObj = idObj;
        this.etape = etape;
        this.priorite = priorite;
    }

    // Constructor simple
    public PlanAction(String etape, int priorite) {
        this.etape = etape;
        this.priorite = priorite;
    }

    // Getters & Setters
    public int getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(int idPlan) {
        this.idPlan = idPlan;
    }

    public int getIdObj() {
        return idObj;
    }

    public void setIdObj(int idObj) {
        this.idObj = idObj;
    }

    public String getEtape() {
        return etape;
    }

    public void setEtape(String etape) {
        this.etape = etape;
    }

    public int getPriorite() {
        return priorite;
    }

    public void setPriorite(int priorite) {
        this.priorite = priorite;
    }

    public String getPrioriteLabel() {
        return switch (priorite) {
            case 1 -> "Haute";
            case 2 -> "Moyenne";
            case 3 -> "Basse";
            default -> "Non définie";
        };
    }

    @Override
    public String toString() {
        return etape + " [" + getPrioriteLabel() + "]";
    }
}
