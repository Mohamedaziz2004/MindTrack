package entities;

import java.time.LocalTime;

public class RappelHabitude {
    private int idRappel;
    private int idHabitude;
    private LocalTime heureRappel;
    private String jours;     // ex: "Lun,Mar,Mer"
    private boolean actif;
    private String message;

    public RappelHabitude() {}

    public RappelHabitude(int idRappel, int idHabitude, LocalTime heureRappel, String jours, boolean actif, String message) {
        this.idRappel = idRappel;
        this.idHabitude = idHabitude;
        this.heureRappel = heureRappel;
        this.jours = jours;
        this.actif = actif;
        this.message = message;
    }

    public RappelHabitude(int idHabitude, LocalTime heureRappel, String jours, boolean actif, String message) {
        this.idHabitude = idHabitude;
        this.heureRappel = heureRappel;
        this.jours = jours;
        this.actif = actif;
        this.message = message;
    }

    public int getIdRappel() { return idRappel; }
    public void setIdRappel(int idRappel) { this.idRappel = idRappel; }

    public int getIdHabitude() { return idHabitude; }
    public void setIdHabitude(int idHabitude) { this.idHabitude = idHabitude; }

    public LocalTime getHeureRappel() { return heureRappel; }
    public void setHeureRappel(LocalTime heureRappel) { this.heureRappel = heureRappel; }

    public String getJours() { return jours; }
    public void setJours(String jours) { this.jours = jours; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "RappelHabitude{" +
                "idRappel=" + idRappel +
                ", idHabitude=" + idHabitude +
                ", heureRappel=" + heureRappel +
                ", jours='" + jours + '\'' +
                ", actif=" + actif +
                ", message='" + message + '\'' +
                '}';
    }
}
