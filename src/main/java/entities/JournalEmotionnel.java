package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JournalEmotionnel {
    private int idJournal;
    private String notePersonnelle;
    private LocalDateTime dateCreation;
    private int idU;  // Only user ID (no mood ID)

    public JournalEmotionnel() {
    }

    public JournalEmotionnel(String notePersonnelle, LocalDateTime dateCreation, int idU) {
        this.notePersonnelle = notePersonnelle;
        this.dateCreation = dateCreation;
        this.idU = idU;
    }

    public JournalEmotionnel(int idJournal, String notePersonnelle, LocalDateTime dateCreation, int idU) {
        this.idJournal = idJournal;
        this.notePersonnelle = notePersonnelle;
        this.dateCreation = dateCreation;
        this.idU = idU;
    }

    // Getters and Setters
    public int getIdJournal() {
        return idJournal;
    }

    public void setIdJournal(int idJournal) {
        this.idJournal = idJournal;
    }

    public String getNotePersonnelle() {
        return notePersonnelle;
    }

    public void setNotePersonnelle(String notePersonnelle) {
        this.notePersonnelle = notePersonnelle;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Helper method to get just the date part
    public LocalDate getDate() {
        return dateCreation != null ? dateCreation.toLocalDate() : null;
    }

    public int getIdU() {
        return idU;
    }

    public void setIdU(int idU) {
        this.idU = idU;
    }

    // Alias for compatibility with getIdJ()
    public int getIdJ() {
        return getIdJournal();
    }

    @Override
    public String toString() {
        return "JournalEmotionnel{" +
                "idJournal=" + idJournal +
                ", notePersonnelle='" + notePersonnelle + '\'' +
                ", dateCreation=" + dateCreation +
                ", idU=" + idU +
                '}';
    }
}
