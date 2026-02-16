package entities;

public class Utilisateur {

    // Attributs (privés = encapsulation)
    private int idU;
    private String nomU;
    private String prenomU;
    private String emailU;
    private String mdpsU;
    private int ageU;

    // 🔹 Constructeur vide (OBLIGATOIRE pour JDBC)
    public Utilisateur() {
    }

    // 🔹 Constructeur sans id (pour insertion)
    public Utilisateur(String nomU, String prenomU, String emailU, String mdpsU, int ageU) {
        this.nomU = nomU;
        this.prenomU = prenomU;
        this.emailU = emailU;
        this.mdpsU = mdpsU;
        this.ageU = ageU;
    }

    // 🔹 Constructeur complet
    public Utilisateur(int idU, String nomU, String prenomU, String emailU, String mdpsU, int ageU) {
        this.idU = idU;
        this.nomU = nomU;
        this.prenomU = prenomU;
        this.emailU = emailU;
        this.mdpsU = mdpsU;
        this.ageU = ageU;
    }

    // Getters & Setters
    public int getIdU() {
        return idU;
    }

    public void setIdU(int idU) {
        this.idU = idU;
    }

    public String getNomU() {
        return nomU;
    }

    public void setNomU(String nomU) {
        this.nomU = nomU;
    }

    public String getPrenomU() {
        return prenomU;
    }

    public void setPrenomU(String prenomU) {
        this.prenomU = prenomU;
    }

    public String getEmailU() {
        return emailU;
    }

    public void setEmailU(String emailU) {
        this.emailU = emailU;
    }

    public String getMdpsU() {
        return mdpsU;
    }

    public void setMdpsU(String mdpsU) {
        this.mdpsU = mdpsU;
    }

    public int getAgeU() {
        return ageU;
    }

    public void setAgeU(int ageU) {
        this.ageU = ageU;
    }
}

