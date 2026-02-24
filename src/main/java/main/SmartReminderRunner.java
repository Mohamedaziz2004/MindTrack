package main;

import entities.RappelHabitude;
import services.HabitudeService;
import services.RappelHabitudeService;
import services.SuiviHabitudeService;
import utils.NotificationUtil;
import utils.Session;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;


public class SmartReminderRunner {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SmartReminderRunner");
        t.setDaemon(true);
        return t;
    });

    private final RappelHabitudeService rappelService = new RappelHabitudeService();
    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();

    // anti-doublon par minute: idRappel|date|HH:mm
    private final Set<String> dejaEnvoyesMinute = ConcurrentHashMap.newKeySet();

    // relances: idRappel|date|count
    private final ConcurrentHashMap<String, Integer> relancesParJour = new ConcurrentHashMap<>();

    // paramètres smart
    private final int relanceEveryMinutes = 30; // relance toutes les 30 min
    private final int maxRelancesPerDay = 4;    // max 4 relances/jour après le rappel initial

    public void start() {
        NotificationUtil.init();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                int idU = Session.getIdU();
                LocalDate today = LocalDate.now();
                LocalTime nowMinute = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

                List<RappelHabitude> rappels = rappelService.rappelsActifsDuUser(idU);

                for (RappelHabitude r : rappels) {
                    if (!rappelService.rappelValideAujourdhui(r, today)) continue;

                    LocalTime rappelMinute = r.getHeureRappel().truncatedTo(ChronoUnit.MINUTES);

                    // 1) rappel initial EXACT à l'heure
                    if (rappelMinute.equals(nowMinute)) {
                        envoyerSiNonComplete(idU, r, today, nowMinute, true);
                        continue;
                    }

                    // 2) smart relance après l'heure du rappel (toutes les X minutes)
                    if (nowMinute.isAfter(rappelMinute)) {
                        long minutesAfter = ChronoUnit.MINUTES.between(rappelMinute, nowMinute);
                        if (minutesAfter > 0 && minutesAfter % relanceEveryMinutes == 0) {
                            envoyerSiNonComplete(idU, r, today, nowMinute, false);
                        }
                    }
                }
            } catch (Exception ignored) {
                // évite de casser le scheduler
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void envoyerSiNonComplete(int idU, RappelHabitude r, LocalDate date, LocalTime nowMinute, boolean isInitial) {
        try {
            boolean doneToday = suiviService.isDoneOnDate(r.getIdHabitude(), date);

            if (doneToday) return; // ✅ smart: si déjà fait => pas de notif

            // anti-doublon minute
            String keyMinute = r.getIdRappel() + "|" + date + "|" + nowMinute;
            if (!dejaEnvoyesMinute.add(keyMinute)) return;

            // limite relances/jour (on ne limite pas le rappel initial)
            if (!isInitial) {
                String keyDay = r.getIdRappel() + "|" + date;
                int c = relancesParJour.getOrDefault(keyDay, 0);
                if (c >= maxRelancesPerDay) return;
                relancesParJour.put(keyDay, c + 1);
            }

            String nomHab = String.valueOf(habService.getById(r.getIdHabitude()));
            String msg = (r.getMessage() == null || r.getMessage().isBlank())
                    ? "⏰ N'oublie pas : " + nomHab
                    : r.getMessage();

            String title = isInitial ? "MindTrack - Rappel" : "MindTrack - Smart reminder";
            NotificationUtil.show(title, msg);

        } catch (Exception ignored) {}
    }

}