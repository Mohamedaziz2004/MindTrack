package entities;

import java.time.LocalDate;

public class SuiviHabitude {
    private int idSuivi;
    private LocalDate date;
    private boolean etat;
    private int valeur;        // ✅ NEW
    private int idHabitude;

    public SuiviHabitude() {}

    // anciens constructeurs (compat)
    public SuiviHabitude(int idSuivi, LocalDate date, boolean etat, int idHabitude) {
        this(idSuivi, date, etat, etat ? 1 : 0, idHabitude);
    }

    public SuiviHabitude(LocalDate date, boolean etat, int idHabitude) {
        this(0, date, etat, etat ? 1 : 0, idHabitude);
    }

    // nouveaux constructeurs
    public SuiviHabitude(int idSuivi, LocalDate date, boolean etat, int valeur, int idHabitude) {
        this.idSuivi = idSuivi;
        this.date = date;
        this.etat = etat;
        this.valeur = valeur;
        this.idHabitude = idHabitude;
    }

    public SuiviHabitude(LocalDate date, boolean etat, int valeur, int idHabitude) {
        this(0, date, etat, valeur, idHabitude);
    }

    public int getIdSuivi() { return idSuivi; }
    public void setIdSuivi(int idSuivi) { this.idSuivi = idSuivi; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isEtat() { return etat; }
    public void setEtat(boolean etat) { this.etat = etat; }

    public int getValeur() { return valeur; }
    public void setValeur(int valeur) { this.valeur = valeur; }

    public int getIdHabitude() { return idHabitude; }
    public void setIdHabitude(int idHabitude) { this.idHabitude = idHabitude; }

    @Override
    public String toString() {
        return "SuiviHabitude{" +
                "idSuivi=" + idSuivi +
                ", date=" + date +
                ", etat=" + etat +
                ", valeur=" + valeur +
                ", idHabitude=" + idHabitude +
                '}';
    }
}