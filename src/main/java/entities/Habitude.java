package entities;

public class Habitude {

    private int idHabitude;
    private String nom;
    private String frequence;
    private String objectif;
    private int idU;

    // ✅ NEW (métier B)
    private String habitType;   // "BOOLEAN", "COUNT", "TIME"
    private int targetValue;    // ex: 8 (glasses) / 20 (min)
    private String unit;        // ex: "glasses", "min"

    public Habitude() {
        this.habitType = "BOOLEAN";
        this.targetValue = 1;
        this.unit = "";
    }

    // anciens constructeurs (compat)
    public Habitude(int idHabitude, String nom, String frequence, String objectif, int idU) {
        this(idHabitude, nom, frequence, objectif, idU, "BOOLEAN", 1, "");
    }

    public Habitude(String nom, String frequence, String objectif, int idU) {
        this(0, nom, frequence, objectif, idU, "BOOLEAN", 1, "");
    }

    // nouveaux constructeurs
    public Habitude(int idHabitude, String nom, String frequence, String objectif, int idU,
                    String habitType, int targetValue, String unit) {
        this.idHabitude = idHabitude;
        this.nom = nom;
        this.frequence = frequence;
        this.objectif = objectif;
        this.idU = idU;
        this.habitType = (habitType == null || habitType.isBlank()) ? "BOOLEAN" : habitType.toUpperCase();
        this.targetValue = (targetValue <= 0) ? 1 : targetValue;
        this.unit = (unit == null) ? "" : unit;
    }

    public Habitude(String nom, String frequence, String objectif, int idU,
                    String habitType, int targetValue, String unit) {
        this(0, nom, frequence, objectif, idU, habitType, targetValue, unit);
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

    public String getHabitType() { return habitType; }
    public void setHabitType(String habitType) {
        this.habitType = (habitType == null || habitType.isBlank()) ? "BOOLEAN" : habitType.toUpperCase();
    }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = Math.max(1, targetValue); }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = (unit == null) ? "" : unit; }

    public boolean isBooleanType() { return "BOOLEAN".equalsIgnoreCase(habitType); }
    public boolean isCountType()   { return "COUNT".equalsIgnoreCase(habitType); }
    public boolean isTimeType()    { return "TIME".equalsIgnoreCase(habitType); }

    public String targetLabel() {
        if (isBooleanType()) return "Done/Not done";
        String u = (unit == null || unit.isBlank()) ? "" : (" " + unit);
        return "Target: " + targetValue + u;
    }

    @Override
    public String toString() {
        // ✅ important pour ComboBox : affichage propre
        return nom;
    }
}