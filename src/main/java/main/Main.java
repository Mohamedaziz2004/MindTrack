package main;

import entities.Habitude;
import entities.RappelHabitude;
import entities.SuiviHabitude;
import services.HabitudeService;
import services.RappelHabitudeService;
import services.SuiviHabitudeService;
import utils.NotificationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;

public class Main {

    private static final Set<String> dejaAffiche = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        HabitudeService hs = new HabitudeService();
        SuiviHabitudeService shs = new SuiviHabitudeService();
        RappelHabitudeService rs = new RappelHabitudeService();
        Scanner sc = new Scanner(System.in);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        try {
            System.out.println("=== MindTrack | Suivi des habitudes + Rappels ===");
            System.out.print("Entrer idU (ex: 1) : ");
            int idU = readInt(sc);

            // ✅ init notif Windows
            NotificationUtil.init();

            // ✅ scheduler en background
            startReminderScheduler(scheduler, rs, idU);

            int choix;
            do {
                afficherMenu();
                System.out.print("Choix : ");
                choix = readInt(sc);

                switch (choix) {

                    // ================= HABITUDES =================
                    case 1 -> { // Ajouter
                        System.out.print("Nom : ");
                        String nom = readLine(sc);
                        System.out.print("Fréquence : ");
                        String freq = readLine(sc);
                        System.out.print("Objectif : ");
                        String obj = readLine(sc);

                        int idHab = hs.ajouterEtRetournerId(new Habitude(nom, freq, obj, idU));
                        System.out.println("✅ Habitude ajoutée. idHabitude = " + idHab);
                    }

                    case 2 -> { // Afficher
                        afficherMesHabitudes(hs, idU);
                    }

                    case 3 -> { // Modifier
                        afficherMesHabitudes(hs, idU);
                        System.out.print("idHabitude à modifier : ");
                        int idHab = readInt(sc);

                        System.out.print("Nouveau nom : ");
                        String nom = readLine(sc);
                        System.out.print("Nouvelle fréquence : ");
                        String freq = readLine(sc);
                        System.out.print("Nouvel objectif : ");
                        String obj = readLine(sc);

                        hs.modifier(new Habitude(idHab, nom, freq, obj, idU));
                        System.out.println("✅ Habitude modifiée.");
                    }

                    case 4 -> { // Supprimer
                        afficherMesHabitudes(hs, idU);
                        System.out.print("idHabitude à supprimer : ");
                        int idHab = readInt(sc);

                        hs.supprimer(idHab);
                        System.out.println("✅ Habitude supprimée.");
                    }

                    // ================= SUIVI =================
                    case 5 -> { // Marquer aujourd’hui
                        afficherMesHabitudes(hs, idU);
                        System.out.print("idHabitude : ");
                        int idHab = readInt(sc);

                        System.out.print("Etat (1=faite, 0=non faite) : ");
                        int et = readInt(sc);

                        shs.marquerCommeFaite(idHab, LocalDate.now(), et == 1);
                        System.out.println("✅ Suivi enregistré pour aujourd’hui.");
                    }

                    case 6 -> { // Historique
                        afficherMesHabitudes(hs, idU);
                        System.out.print("idHabitude : ");
                        int idHab = readInt(sc);

                        List<SuiviHabitude> hist = shs.historiqueParHabitude(idHab);
                        System.out.println("📌 Historique :");
                        if (hist.isEmpty()) System.out.println("(Aucun suivi)");
                        else hist.forEach(System.out::println);
                    }

                    case 7 -> { // Stats
                        afficherMesHabitudes(hs, idU);
                        System.out.print("idHabitude : ");
                        int idHab = readInt(sc);

                        int streak = shs.getStreakActuel(idHab);
                        double tauxWeek = shs.getTauxReussiteSemaine(idHab);
                        double tauxMonth = shs.getTauxReussiteMois(idHab);

                        System.out.println("🔥 Streak actuel = " + streak + " jours");
                        System.out.printf("📊 Taux semaine = %.2f%%\n", tauxWeek);
                        System.out.printf("📈 Taux mois = %.2f%%\n", tauxMonth);
                    }

                    // ================= RAPPELS =================
                    case 8 -> { // Ajouter un rappel (user programme)
                        afficherMesHabitudes(hs, idU);

                        System.out.print("idHabitude : ");
                        int idHab = readInt(sc);

                        System.out.print("Heure (HH:mm) ex: 20:00 : ");
                        LocalTime heure = LocalTime.parse(readLine(sc)); // "20:00"

                        System.out.print("Jours (ex: Lun,Mar,Mer,Jeu,Ven,Sam,Dim) : ");
                        String jours = readLine(sc);

                        System.out.print("Message du rappel : ");
                        String msg = readLine(sc);

                        rs.ajouter(new RappelHabitude(idHab, heure, jours, true, msg));
                        System.out.println("✅ Rappel ajouté ! (le scheduler est déjà actif)");
                    }

                    case 9 -> { // Afficher rappels actifs
                        List<RappelHabitude> rappels = rs.rappelsActifsDuUser(idU);
                        System.out.println("📌 Mes rappels actifs :");
                        if (rappels.isEmpty()) System.out.println("(Aucun rappel actif)");
                        else rappels.forEach(System.out::println);
                    }

                    case 0 -> System.out.println("✅ Au revoir !");
                    default -> System.out.println("❌ Choix invalide.");
                }

            } while (choix != 0);

        } catch (Exception e) {
            System.out.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        } finally {
            scheduler.shutdownNow();
            sc.close();
        }
    }

    // ✅ Scheduler en background (notif Windows + console)
    private static void startReminderScheduler(ScheduledExecutorService scheduler, RappelHabitudeService rs, int idU) {
        System.out.println("🔔 Scheduler démarré (idU=" + idU + ") ...");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDate today = LocalDate.now();
                LocalTime nowMinute = LocalTime.now().withSecond(0).withNano(0);

                List<RappelHabitude> rappels = rs.rappelsActifsDuUser(idU);

                for (RappelHabitude r : rappels) {
                    if (!rs.rappelValideAujourdhui(r, today)) continue;

                    LocalTime rappelMinute = r.getHeureRappel().withSecond(0).withNano(0);

                    if (rappelMinute.equals(nowMinute)) {
                        String key = r.getIdRappel() + "_" + today + "_" + nowMinute;

                        if (dejaAffiche.add(key)) {
                            String msg = (r.getMessage() == null || r.getMessage().isBlank())
                                    ? "⏰ Rappel: tu as une habitude à faire !"
                                    : r.getMessage();

                            NotificationUtil.show("MindTrack - Rappel", msg);
                            System.out.println("🔔 NOTIF envoyée [" + nowMinute + "] => " + msg);
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("❌ ERREUR Scheduler: " + ex.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    // ✅ Ton menu EXACT
    private static void afficherMenu() {
        System.out.println("\n-----------------------------");
        System.out.println("1) Ajouter une habitude");
        System.out.println("2) Afficher mes habitudes");
        System.out.println("3) Modifier une habitude");
        System.out.println("4) Supprimer une habitude");
        System.out.println("5) Marquer une habitude (faite/non faite) aujourd’hui");
        System.out.println("6) Historique d’une habitude");
        System.out.println("7) Stats (Streak + Taux semaine/mois)");
        System.out.println("8) Ajouter un rappel");
        System.out.println("9) Afficher mes rappels actifs");
        System.out.println("0) Quitter");
        System.out.println("-----------------------------");
    }

    private static void afficherMesHabitudes(HabitudeService hs, int idU) throws Exception {
        List<Habitude> list = hs.afficherParUser(idU);
        System.out.println("📌 Mes habitudes :");
        if (list.isEmpty()) System.out.println("(Aucune habitude)");
        else list.forEach(System.out::println);
    }

    private static int readInt(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                System.out.print("Entrer un nombre valide : ");
            }
        }
    }

    private static String readLine(Scanner sc) {
        String s = sc.nextLine().trim();
        while (s.isEmpty()) {
            System.out.print("Champ vide, réessaye : ");
            s = sc.nextLine().trim();
        }
        return s;
    }
}
