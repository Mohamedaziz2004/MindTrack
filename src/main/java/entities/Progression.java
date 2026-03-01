package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Progression {
    private int idProgression;
    private Integer idJalon;
    private LocalDateTime dateRealisation;
    private Integer scoreObtenu; // 0-100, NULL pour exercices sans score
    private Integer ressentiUtilisateur; // 1-10
    private String notesPersonnelles;
    private int tempsPasse; // en secondes
    private int idUser;
    private Integer idExercice;
    private Integer idSession;
    private boolean atteint;
    private LocalDateTime dateAtteinte;
    private int pourcentageProgression;

    // Relations
    private Exercice exercice;
    private Session session;

    public Progression() {
        this.dateRealisation = LocalDateTime.now();
    }

    public Progression(int idUser, Exercice exercice) {
        this.idUser = idUser;
        this.exercice = exercice;
        this.idExercice = exercice != null ? exercice.getIdExercice() : null;
        this.dateRealisation = LocalDateTime.now();
    }

    // Getters and Setters
    public int getIdProgression() { return idProgression; }
    public void setIdProgression(int idProgression) { this.idProgression = idProgression; }

    public Integer getIdJalon() { return idJalon; }
    public void setIdJalon(Integer idJalon) { this.idJalon = idJalon; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }

    public Integer getScoreObtenu() { return scoreObtenu; }
    public void setScoreObtenu(Integer scoreObtenu) {
        if (scoreObtenu != null && (scoreObtenu < 0 || scoreObtenu > 100)) {
            throw new IllegalArgumentException("Le score doit être entre 0 et 100");
        }
        this.scoreObtenu = scoreObtenu;
    }

    public Integer getRessentiUtilisateur() { return ressentiUtilisateur; }
    public void setRessentiUtilisateur(Integer ressentiUtilisateur) {
        if (ressentiUtilisateur != null && (ressentiUtilisateur < 1 || ressentiUtilisateur > 10)) {
            throw new IllegalArgumentException("Le ressenti doit être entre 1 et 10");
        }
        this.ressentiUtilisateur = ressentiUtilisateur;
    }

    public String getNotesPersonnelles() { return notesPersonnelles; }
    public void setNotesPersonnelles(String notesPersonnelles) { this.notesPersonnelles = notesPersonnelles; }

    public int getTempsPasse() { return tempsPasse; }
    public void setTempsPasse(int tempsPasse) { this.tempsPasse = tempsPasse; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public Integer getIdExercice() { return idExercice; }
    public void setIdExercice(Integer idExercice) { this.idExercice = idExercice; }

    public Integer getIdSession() { return idSession; }
    public void setIdSession(Integer idSession) { this.idSession = idSession; }

    public boolean isAtteint() { return atteint; }
    public void setAtteint(boolean atteint) { this.atteint = atteint; }

    public LocalDateTime getDateAtteinte() { return dateAtteinte; }
    public void setDateAtteinte(LocalDateTime dateAtteinte) { this.dateAtteinte = dateAtteinte; }

    public int getPourcentageProgression() { return pourcentageProgression; }
    public void setPourcentageProgression(int pourcentageProgression) { this.pourcentageProgression = pourcentageProgression; }

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) {
        this.exercice = exercice;
        if (exercice != null) {
            this.idExercice = exercice.getIdExercice();
        }
    }

    public Session getSession() { return session; }
    public void setSession(Session session) {
        this.session = session;
        if (session != null) {
            this.idSession = session.getIdSession();
        }
    }

    // Business methods
    public boolean estReussi() {
        return scoreObtenu != null && scoreObtenu >= 70;
    }

    public String evaluerBienEtre() {
        if (ressentiUtilisateur == null) return "Non évalué";
        if (ressentiUtilisateur >= 8) return "Excellent 😊";
        if (ressentiUtilisateur >= 6) return "Bon 🙂";
        if (ressentiUtilisateur >= 4) return "Moyen 😐";
        return "À améliorer 😔";
    }

    public void afficherResume() {
        System.out.println("══════════════════════════════");
        System.out.println("📊 PROGRESSION #" + idProgression);
        System.out.println("══════════════════════════════");
        System.out.println("📅 Date: " + dateRealisation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        System.out.println("🏋️ Exercice: " + (exercice != null ? exercice.getNom() : "Inconnu"));
        if (scoreObtenu != null) {
            System.out.println("📈 Score: " + scoreObtenu + "% " + (estReussi() ? "✅ Réussi" : "📝 À améliorer"));
        }
        if (ressentiUtilisateur != null) {
            System.out.println("😊 Bien-être: " + evaluerBienEtre());
        }
        if (notesPersonnelles != null && !notesPersonnelles.isEmpty()) {
            System.out.println("📝 Notes: " + notesPersonnelles);
        }
        int minutes = tempsPasse / 60;
        int secondes = tempsPasse % 60;
        System.out.println("⏱️ Temps: " + minutes + "m " + secondes + "s");
        System.out.println("══════════════════════════════");
    }

    @Override
    public String toString() {
        String date = dateRealisation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String exo = exercice != null ? exercice.getNom() : "?";
        if (scoreObtenu != null) {
            return date + " - " + exo + " - Score: " + scoreObtenu + "%";
        }
        return date + " - " + exo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Progression that = (Progression) o;
        return idProgression == that.idProgression;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProgression);
    }
}