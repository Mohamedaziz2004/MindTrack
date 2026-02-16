package main;

import entities.RappelHabitude;
import services.RappelHabitudeService;
import utils.NotificationUtil;
import utils.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.*;

public class ReminderRunner {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final RappelHabitudeService rappelService = new RappelHabitudeService();

    // anti-doublon: idRappel|date|HH:mm
    private final Set<String> dejaEnvoyes = ConcurrentHashMap.newKeySet();

    public void start() {
        NotificationUtil.init();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                int idU = Session.getIdU();
                LocalDate today = LocalDate.now();
                LocalTime nowMinute = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

                for (RappelHabitude r : rappelService.rappelsActifsDuUser(idU)) {
                    if (!rappelService.rappelValideAujourdhui(r, today)) continue;

                    LocalTime rappelMinute = r.getHeureRappel().truncatedTo(ChronoUnit.MINUTES);
                    if (!rappelMinute.equals(nowMinute)) continue;

                    String key = r.getIdRappel() + "|" + today + "|" + nowMinute;
                    if (dejaEnvoyes.add(key)) {
                        NotificationUtil.show("MindTrack - Rappel", r.getMessage());
                    }
                }
            } catch (Exception ignored) {}
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
