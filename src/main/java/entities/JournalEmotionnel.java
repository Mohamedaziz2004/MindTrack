package entities;

import java.time.LocalDate;

public class JournalEmotionnel {
    private int idJournal;
    private String notePersonnelle;
    private LocalDate dateCreation;
    private int idU;  // Only user ID (no mood ID)

    public JournalEmotionnel() {
    }

    public JournalEmotionnel(String notePersonnelle, LocalDate dateCreation, int idU) {
        this.notePersonnelle = notePersonnelle;
        this.dateCreation = dateCreation;
        this.idU = idU;
    }

    public JournalEmotionnel(int idJournal, String notePersonnelle, LocalDate dateCreation, int idU) {
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

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getIdU() {
        return idU;
    }

    public void setIdU(int idU) {
        this.idU = idU;
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
