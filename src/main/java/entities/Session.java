package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Session {
    private int idSession;
    private LocalDate dateSession;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String resultat;
    private String commentaires;
    private Integer dureeReelle; // en secondes
    private boolean terminee;
    private int idUser;
    private int idExercice;

    // Relations
    private Exercice exercice;
    private Progression progression;

    public Session() {
        this.dateSession = LocalDate.now();
        this.terminee = false;
    }

    public Session(Exercice exercice) {
        this.exercice = exercice;
        this.idExercice = exercice != null ? exercice.getIdExercice() : 0;
        this.dateSession = LocalDate.now();
        this.terminee = false;
    }

    // Getters and Setters
    public int getIdSession() { return idSession; }
    public void setIdSession(int idSession) { this.idSession = idSession; }

    public LocalDate getDateSession() { return dateSession; }
    public void setDateSession(LocalDate dateSession) { this.dateSession = dateSession; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getResultat() { return resultat; }
    public void setResultat(String resultat) { this.resultat = resultat; }

    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }

    public Integer getDureeReelle() { return dureeReelle; }
    public void setDureeReelle(Integer dureeReelle) { this.dureeReelle = dureeReelle; }

    public boolean isTerminee() { return terminee; }
    public void setTerminee(boolean terminee) { this.terminee = terminee; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public int getIdExercice() { return idExercice; }
    public void setIdExercice(int idExercice) { this.idExercice = idExercice; }

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) {
        this.exercice = exercice;
        if (exercice != null) {
            this.idExercice = exercice.getIdExercice();
        }
    }

    public Progression getProgression() { return progression; }
    public void setProgression(Progression progression) { this.progression = progression; }

    // Business methods
    public void demarrer() {
        this.dateDebut = LocalDateTime.now();
        this.terminee = false;
        System.out.println("▶ Session démarrée à " + dateDebut.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    public void terminer() {
        this.dateFin = LocalDateTime.now();
        this.terminee = true;
        if (dateDebut != null) {
            this.dureeReelle = (int) Duration.between(dateDebut, dateFin).getSeconds();
        }
        System.out.println("⏹️ Session terminée à " + dateFin.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        if (dureeReelle != null) {
            System.out.println("⏱️ Durée réelle: " + (dureeReelle / 60) + "m " + (dureeReelle % 60) + "s");
        }
    }

    public void enregistrerResultat(String resultat, String commentaires) {
        this.resultat = resultat;
        this.commentaires = commentaires;
        this.terminee = true;
        if (dateDebut == null && dateFin == null) {
            this.dateDebut = dateSession.atStartOfDay();
            this.dateFin = LocalDateTime.now();
        }
    }

    public String getStatut() {
        if (terminee) return "Terminée";
        if (dateDebut != null && dateFin == null) return "En cours";
        return "Planifiée";
    }

    public String getResume() {
        String exoNom = exercice != null ? exercice.getNom() : "Inconnu";
        String date = dateSession.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return String.format("Session #%d - %s - %s - %s", idSession, date, exoNom, getStatut());
    }

    @Override
    public String toString() {
        return "Session #" + idSession + " - " + dateSession + " - " + getStatut();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return idSession == session.idSession;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSession);
    }
}