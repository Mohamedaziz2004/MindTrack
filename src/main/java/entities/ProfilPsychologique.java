package entities;

public class ProfilPsychologique {


    private int idP;
    private int niveauStress;
    private int niveauMotivation;
    private String description;

    private Utilisateur utilisateur;

    public ProfilPsychologique() {
    }


    public ProfilPsychologique(int niveauStress, int niveauMotivation, String description, Utilisateur utilisateur) {
        this.niveauStress = niveauStress;
        this.niveauMotivation = niveauMotivation;
        this.description = description;
        this.utilisateur = utilisateur;
    }


    public ProfilPsychologique(int idP, int niveauStress, int niveauMotivation, String description, Utilisateur utilisateur) {
        this.idP = idP;
        this.niveauStress = niveauStress;
        this.niveauMotivation = niveauMotivation;
        this.description = description;
        this.utilisateur = utilisateur;
    }

    // Getters & Setters
    public int getIdP() {
        return idP;
    }

    public void setIdP(int idP) {
        this.idP = idP;
    }

    public int getNiveauStress() {
        return niveauStress;
    }

    public void setNiveauStress(int niveauStress) {
        this.niveauStress = niveauStress;
    }

    public int getNiveauMotivation() {
        return niveauMotivation;
    }

    public void setNiveauMotivation(int niveauMotivation) {
        this.niveauMotivation = niveauMotivation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}

