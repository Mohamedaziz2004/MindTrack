package entities;

import java.time.LocalDate;

public class SuiviHabitude {
    private int idSuivi;
    private LocalDate date;
    private boolean etat;
    private int idHabitude;

    public SuiviHabitude() {}

    public SuiviHabitude(int idSuivi, LocalDate date, boolean etat, int idHabitude) {
        this.idSuivi = idSuivi;
        this.date = date;
        this.etat = etat;
        this.idHabitude = idHabitude;
    }

    public SuiviHabitude(LocalDate date, boolean etat, int idHabitude) {
        this.date = date;
        this.etat = etat;
        this.idHabitude = idHabitude;
    }

    public int getIdSuivi() { return idSuivi; }
    public void setIdSuivi(int idSuivi) { this.idSuivi = idSuivi; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isEtat() { return etat; }
    public void setEtat(boolean etat) { this.etat = etat; }
    public int getIdHabitude() { return idHabitude; }
    public void setIdHabitude(int idHabitude) { this.idHabitude = idHabitude; }

    @Override
    public String toString() {
        return "SuiviHabitude{" +
                "idSuivi=" + idSuivi +
                ", date=" + date +
                ", etat=" + etat +
                ", idHabitude=" + idHabitude +
                '}';
    }
}
