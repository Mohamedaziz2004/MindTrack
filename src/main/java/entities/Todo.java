package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Todo {
    private int idTodo;
    private String titre;
    private String description;
    private String statut; // TODO, EN_COURS, DONE
    private String priorite; // BASSE, MOYENNE, HAUTE, URGENTE
    private LocalDateTime dateCreation;
    private LocalDate dateEcheance;
    private LocalDateTime dateCompletion;
    private Integer idExercice;
    private Integer tempsEstime;
    private Integer progression;
    private String notes;
    private String couleur;

    // Relations
    private Exercice exercice;

    public Todo() {
        this.dateCreation = LocalDateTime.now();
        this.statut = "TODO";
        this.priorite = "MOYENNE";
        this.progression = 0;
        this.couleur = "#3498db";
    }

    public Todo(String titre) {
        this.titre = titre;
        this.dateCreation = LocalDateTime.now();
        this.statut = "TODO";
        this.priorite = "MOYENNE";
        this.progression = 0;
        this.couleur = "#3498db";
    }

    // Getters et Setters
    public int getIdTodo() { return idTodo; }
    public void setIdTodo(int idTodo) { this.idTodo = idTodo; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public LocalDateTime getDateCompletion() { return dateCompletion; }
    public void setDateCompletion(LocalDateTime dateCompletion) { this.dateCompletion = dateCompletion; }

    public Integer getIdExercice() { return idExercice; }
    public void setIdExercice(Integer idExercice) { this.idExercice = idExercice; }

    public Integer getTempsEstime() { return tempsEstime; }
    public void setTempsEstime(Integer tempsEstime) { this.tempsEstime = tempsEstime; }

    public Integer getProgression() { return progression; }
    public void setProgression(Integer progression) { this.progression = progression; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) {
        this.exercice = exercice;
        if (exercice != null) {
            this.idExercice = exercice.getIdExercice();
        }
    }

    // Méthodes utilitaires
    public boolean isTodo() { return "TODO".equals(statut); }
    public boolean isEnCours() { return "EN_COURS".equals(statut); }
    public boolean isDone() { return "DONE".equals(statut); }

    public void marquerEnCours() {
        this.statut = "EN_COURS";
    }

    public void marquerTermine() {
        this.statut = "DONE";
        this.dateCompletion = LocalDateTime.now();
        this.progression = 100;
    }

    public String getPrioriteColor() {
        switch (priorite) {
            case "BASSE": return "#27AE60";
            case "MOYENNE": return "#F39C12";
            case "HAUTE": return "#E74C3C";
            case "URGENTE": return "#8E44AD";
            default: return "#7F8C8D";
        }
    }

    public String getPrioriteIcone() {
        switch (priorite) {
            case "BASSE": return "⬇️";
            case "MOYENNE": return "➡️";
            case "HAUTE": return "⬆️";
            case "URGENTE": return "⚡";
            default: return "•";
        }
    }

    public String getStatutIcone() {
        switch (statut) {
            case "TODO": return "⭕";
            case "EN_COURS": return "🔄";
            case "DONE": return "✅";
            default: return "❓";
        }
    }

    public boolean estEnRetard() {
        return dateEcheance != null &&
                dateEcheance.isBefore(LocalDate.now()) &&
                !isDone();
    }

    public String getTempsRestantFormate() {
        if (dateEcheance == null) return "Pas d'échéance";

        LocalDate today = LocalDate.now();
        long jours = java.time.temporal.ChronoUnit.DAYS.between(today, dateEcheance);

        if (isDone()) return "Terminé";
        if (jours < 0) return "En retard de " + Math.abs(jours) + " jour(s)";
        if (jours == 0) return "Aujourd'hui";
        if (jours == 1) return "Demain";
        return jours + " jours restants";
    }

    @Override
    public String toString() {
        String exo = exercice != null ? " (" + exercice.getNom() + ")" : "";
        return getStatutIcone() + " " + titre + exo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return idTodo == todo.idTodo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTodo);
    }
}