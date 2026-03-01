package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Exercice {
    private int idExercice;
    private String nom;
    private String type; // enum in DB: Méditation, Respiration, Yoga, Étirement
    private int duree; // en minutes
    private String difficulte; // enum in DB: Débutant, Intermédiaire, Avancé, Expert
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String demarche;


    // Relations
    private List<Session> sessions = new ArrayList<>();
    private List<Progression> progressions = new ArrayList<>();

    public Exercice() {
        this.dateCreation = LocalDateTime.now();
    }

    public Exercice(String nom, String type, int duree, String difficulte, String description) {
        this.nom = nom;
        this.type = type;
        this.duree = duree;
        this.difficulte = difficulte;
        this.description = description;
        this.dateCreation = LocalDateTime.now();
    }

    // Getters and Setters
    public int getIdExercice() { return idExercice; }
    public void setIdExercice(int idExercice) { this.idExercice = idExercice; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getDifficulte() { return difficulte; }
    public void setDifficulte(String difficulte) { this.difficulte = difficulte; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }

    public List<Progression> getProgressions() { return progressions; }
    public void setProgressions(List<Progression> progressions) { this.progressions = progressions; }

    public void addSession(Session session) {
        this.sessions.add(session);
        session.setExercice(this);
    }

    public void addProgression(Progression progression) {
        this.progressions.add(progression);
        progression.setExercice(this);
    }

    public double getTauxCompletion() {
        if (progressions.isEmpty()) return 0;
        long reussies = progressions.stream()
                .filter(p -> p.getScoreObtenu() != null && p.getScoreObtenu() >= 70)
                .count();
        return (reussies * 100.0) / progressions.size();
    }

    @Override
    public String toString() {
        return nom + " (" + type + ", " + duree + " min)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exercice exercice = (Exercice) o;
        return idExercice == exercice.idExercice;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idExercice);
    }

    public String getDemarche() {
        return demarche;
    }

    public void setDemarche(String demarche) {
        this.demarche = demarche;
    }
}