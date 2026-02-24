package services;

import entities.Habitude;
import entities.RappelHabitude;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class SmartReminderService {

    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();
    private final RappelHabitudeService rappelService = new RappelHabitudeService();

    private static final int MISS_THRESHOLD = 3;
    private static final LocalTime DEFAULT_TIME = LocalTime.of(20, 0);
    private static final String EVERYDAY = "Lun,Mar,Mer,Jeu,Ven,Sam,Dim";

    public void evaluateAll(List<Habitude> habits) {
        if (habits == null) return;
        for (Habitude h : habits) evaluateHabit(h);
    }

    public void evaluateHabit(Habitude h) {
        if (h == null) return;

        try {
            boolean doneToday = suiviService.isDoneOnDate(h.getIdHabitude(), java.time.LocalDate.now());
            RappelHabitude auto = rappelService.getAutoByHabitude(h.getIdHabitude());

            // ✅ si aujourd'hui fait => désactiver auto
            if (doneToday) {
                if (auto != null && auto.isActif()) {
                    rappelService.setActif(auto.getIdRappel(), false);
                }
                return;
            }

            // ✅ sinon si 3 jours consécutifs ratés => activer/créer auto
            int misses = suiviService.getConsecutiveMisses(h.getIdHabitude(), 7);
            if (misses >= MISS_THRESHOLD) {
                if (auto == null) {
                    String msg = "[AUTO] Tu as manqué " + misses + " jours pour \"" + h.getNom() + "\". Reprends aujourd’hui !";
                    rappelService.ajouter(new RappelHabitude(
                            h.getIdHabitude(),
                            DEFAULT_TIME,
                            EVERYDAY,
                            true,
                            msg,
                            true
                    ));
                } else if (!auto.isActif()) {
                    rappelService.setActif(auto.getIdRappel(), true);
                }
            }
        } catch (SQLException ignored) {}
    }
}