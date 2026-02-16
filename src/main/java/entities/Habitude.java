package entities;

public class Habitude {
    private int idHabitude;
    private String nom;
    private String frequence;
    private String objectif;
    private int idU;

    public Habitude() {}

    public Habitude(int idHabitude, String nom, String frequence, String objectif, int idU) {
        this.idHabitude = idHabitude;
        this.nom = nom;
        this.frequence = frequence;
        this.objectif = objectif;
        this.idU = idU;
    }

    public Habitude(String nom, String frequence, String objectif, int idU) {
        this.nom = nom;
        this.frequence = frequence;
        this.objectif = objectif;
        this.idU = idU;
    }

    public int getIdHabitude() { return idHabitude; }
    public void setIdHabitude(int idHabitude) { this.idHabitude = idHabitude; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }
    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) { this.objectif = objectif; }
    public int getIdU() { return idU; }
    public void setIdU(int idU) { this.idU = idU; }

    @Override
    public String toString() {
        return "Habitude{" +
                "idHabitude=" + idHabitude +
                ", nom='" + nom + '\'' +
                ", frequence='" + frequence + '\'' +
                ", objectif='" + objectif + '\'' +
                ", idU=" + idU +
                '}';
    }
}
