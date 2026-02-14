package entities;

import java.time.LocalDate;
import java.util.Objects;

public class humeur {
    private int idH;
    private LocalDate date;
    private String TypeHumeur;
    private int intensite;
    private int idU;


    public static final String[] MOOD_TYPES = {"Happy", "Calm", "Neutral", "Sad", "Anxious"};

    public humeur(int idH, LocalDate date, String TypeHumeur, int intensite, int idU) {
        this.idH = idH;
        this.date = date;
        setTypeHumeur(TypeHumeur);
        setIntensite(intensite);
        this.idU = idU;
    }

    public humeur(LocalDate date, String TypeHumeur, int intensite, int idU) {
        this.date = date;
        setTypeHumeur(TypeHumeur);
        setIntensite(intensite);
        this.idU = idU;
    }

    public humeur() {
    }


    public int getIdH() {
        return idH;
    }

    public int getIdU() {
        return idU;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTypeHumeur() {
        return TypeHumeur;
    }

    public int getIntensite() {
        return intensite;
    }


    public void setIdH(int idH) {
        this.idH = idH;
    }

    public void setIdU(int idU) {
        this.idU = idU;
    }

    public void setDate(LocalDate date) {
        this.date = date != null ? date : LocalDate.now();
    }

    public void setTypeHumeur(String TypeHumeur) {
        if (TypeHumeur == null || TypeHumeur.trim().isEmpty()) {
            throw new IllegalArgumentException("TypeHumeur cannot be null or empty");
        }

        String validType = validateMoodType(TypeHumeur);
        this.TypeHumeur = validType;
    }

    public void setIntensite(int intensite) {
        if (intensite < 1 || intensite > 10) {
            throw new IllegalArgumentException("Intensite must be between 1 and 10");
        }
        this.intensite = intensite;
    }


    private String validateMoodType(String mood) {
        String normalized = mood.trim();
        for (String validMood : MOOD_TYPES) {
            if (validMood.equalsIgnoreCase(normalized)) {
                return validMood;
            }
        }

        return "Neutral";
    }

    @Override
    public String toString() {
        return "humeur{" +
                "idH=" + idH +
                ", date=" + date +
                ", TypeHumeur='" + TypeHumeur + '\'' +
                ", intensite=" + intensite +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof humeur)) return false;
        humeur other = (humeur) o;
        return idH == other.idH;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idH);
    }
}
