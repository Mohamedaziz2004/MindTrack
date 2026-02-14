package main;

import entities.humeur;
import entities.JournalEmotionnel;
import servives.humeurService;
import servives.JournalService;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.Comparator;

public class MenuHandler {
    private static final humeurService humeurService = new humeurService();
    private static final JournalService journalService = new JournalService();
    private static final Scanner sc = new Scanner(System.in);
    private static final int DEFAULT_USER_ID = 1;

    private static void displayMenuOptions(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
    }

    private static int getUserChoice() {
        int choice = -1;
        try {
            choice = sc.nextInt();
            sc.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine();
        }
        return choice;
    }

    public static void handleMainMenu() {
        String[] mainMenuOptions = {
            "Gérer les Humeurs",
            "Gérer le Journal Emotionnel",
            "Analyse IA des Journaux",
            "Statistiques",
            "Quitter"
        };

        int choix;
        do {
            System.out.println("\n===== MENU PRINCIPAL =====");
            displayMenuOptions(mainMenuOptions);

            choix = getUserChoice();

            switch (choix) {
                case 1:
                    menuHumeur();
                    break;
                case 2:
                    menuJournal();
                    break;
                case 3:
                    menuAnalyseJournaux();
                    break;
                case 4:
                    StatistiquesHandler.afficherStatistiques();
                    break;
                case 5:
                    System.out.println("Merci d'avoir utilisé Mind Track. À bientôt!");
                    break;
                default:
                    System.out.println("Choix invalide!");
            }
        } while (choix != 5);
    }

    public static void menuHumeur() {
        int choix;
        do {
            System.out.println("\n===== MENU HUMEUR =====");
            System.out.println("1. Ajouter une humeur");
            System.out.println("2. Afficher toutes les humeurs");
            System.out.println("3. Modifier une humeur");
            System.out.println("4. Supprimer une humeur");
            System.out.println("5. Rechercher une humeur par ID");
            System.out.println("0. Retour au menu principal");
            System.out.print("Votre choix: ");

            try {
                choix = sc.nextInt();
                sc.nextLine();

                switch (choix) {
                    case 1:
                        ajouterHumeur();
                        break;
                    case 2:
                        afficherHumeurs();
                        break;
                    case 3:
                        modifierHumeur();
                        break;
                    case 4:
                        supprimerHumeur();
                        break;
                    case 5:
                        rechercherHumeurParId();
                        break;
                }
            } catch (Exception e) {
                System.out.println(" Erreur: " + e.getMessage());
                sc.nextLine();
                choix = -1;
            }
        } while (choix != 0);
    }

    public static void ajouterHumeur() {
        try {
            System.out.println("\n--- Ajouter une Humeur ---");
            System.out.println("Choisissez votre humeur:");
            System.out.println("1. Happy");
            System.out.println("2. Calm");
            System.out.println("3. Neutral");
            System.out.println("4. Sad");
            System.out.println("5. Anxious");
            System.out.print("Votre choix (1-5): ");

            int moodChoice = 0;
            String type = "";

            while (moodChoice < 1 || moodChoice > 5) {
                try {
                    moodChoice = sc.nextInt();
                    sc.nextLine();

                    switch (moodChoice) {
                        case 1:
                            type = "Happy";
                            break;
                        case 2:
                            type = "Calm";
                            break;
                        case 3:
                            type = "Neutral";
                            break;
                        case 4:
                            type = "Sad";
                            break;
                        case 5:
                            type = "Anxious";
                            break;
                        default:
                            System.out.println(" Choix invalide. Veuillez choisir entre 1 et 5.");
                            System.out.print("Votre choix (1-5): ");
                            moodChoice = 0;
                    }
                } catch (Exception e) {
                    System.out.println(" Veuillez entrer un nombre valide.");
                    sc.nextLine();
                    moodChoice = 0;
                }
            }

            int intensite = 0;
            while (intensite < 1 || intensite > 10) {
                System.out.print("Intensité (1-10): ");
                try {
                    intensite = sc.nextInt();
                    sc.nextLine();
                    if (intensite < 1 || intensite > 10) {
                        System.out.println(" L'intensité doit être entre 1 et 10.");
                    }
                } catch (Exception e) {
                    System.out.println(" Veuillez entrer un nombre valide.");
                    sc.nextLine();
                }
            }

            humeur h = new humeur(LocalDate.now(), type, intensite, DEFAULT_USER_ID);
            humeurService.create(h);
            System.out.println(" Humeur '" + type + "' ajoutée avec succès! ID: " + h.getIdH());

        } catch (Exception e) {
            System.out.println(" Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    public static void afficherHumeurs() {
        try {
            List<humeur> list = humeurService.readAll();
            list.sort(Comparator.comparingInt(humeur::getIdH));

            System.out.println("\n=== Liste des Humeurs (" + list.size() + ") ===");
            if (list.isEmpty()) {
                System.out.println("Aucune humeur enregistrée.");
            } else {
                for (humeur hu : list) {
                    System.out.println("ID: " + hu.getIdH() +
                            " | Date: " + hu.getDate() +
                            " | Humeur: " + hu.getTypeHumeur() +
                            " | Intensité: " + hu.getIntensite() + "/10" +
                            " | User ID: " + hu.getIdU());
                }
            }
        } catch (Exception e) {
            System.out.println(" Erreur lors de la récupération: " + e.getMessage());
        }
    }

    public static void modifierHumeur() {
        try {
            System.out.println("\n=== MODIFIER UNE HUMEUR ===");
            System.out.print("ID de l'humeur à modifier: ");
            int idUpdate = sc.nextInt();
            sc.nextLine();

            humeur humeurToUpdate = humeurService.read(idUpdate);
            if (humeurToUpdate == null) {
                System.out.println(" Humeur non trouvée.");
                return;
            }

            System.out.println("\n Humeur actuelle:");
            System.out.println("   Type: " + humeurToUpdate.getTypeHumeur());
            System.out.println("   Intensité: " + humeurToUpdate.getIntensite() + "/10");

            System.out.println("\n NOUVELLE HUMEUR:");
            System.out.println("1. Happy");
            System.out.println("2. Calm");
            System.out.println("3. Neutral");
            System.out.println("4. Sad");
            System.out.println("5. Anxious");
            System.out.print("Choisissez la nouvelle humeur (1-5): ");

            int moodChoice = 0;
            String newType = "";

            while (moodChoice < 1 || moodChoice > 5) {
                try {
                    moodChoice = sc.nextInt();
                    sc.nextLine();

                    switch (moodChoice) {
                        case 1:
                            newType = "Happy";
                            break;
                        case 2:
                            newType = "Calm";
                            break;
                        case 3:
                            newType = "Neutral";
                            break;
                        case 4:
                            newType = "Sad";
                            break;
                        case 5:
                            newType = "Anxious";
                            break;
                        default:
                            System.out.println(" Choix invalide! Veuillez choisir entre 1 et 5.");
                            System.out.print("Choisissez une humeur (1-5): ");
                            moodChoice = 0;
                    }
                } catch (Exception e) {
                    System.out.println(" Veuillez entrer un nombre valide.");
                    sc.nextLine();
                    System.out.print("Choisissez une humeur (1-5): ");
                    moodChoice = 0;
                }
            }

            System.out.print("\nNouvelle intensité (1-10): ");
            int newIntensite = 0;
            while (newIntensite < 1 || newIntensite > 10) {
                try {
                    newIntensite = sc.nextInt();
                    sc.nextLine();
                    if (newIntensite < 1 || newIntensite > 10) {
                        System.out.println(" L'intensité doit être entre 1 et 10.");
                        System.out.print("Nouvelle intensité (1-10): ");
                    }
                } catch (Exception e) {
                    System.out.println(" Veuillez entrer un nombre valide.");
                    sc.nextLine();
                    System.out.print("Nouvelle intensité (1-10): ");
                }
            }

            humeurToUpdate.setTypeHumeur(newType);
            humeurToUpdate.setIntensite(newIntensite);
            humeurToUpdate.setDate(LocalDate.now());

            humeurService.update(humeurToUpdate);
            System.out.println("\n Humeur modifiée avec succès!");
            System.out.println("   Nouveau type: " + newType);
            System.out.println("   Nouvelle intensité: " + newIntensite + "/10");

        } catch (Exception e) {
            System.out.println(" Erreur: " + e.getMessage());
        }
    }

    public static void supprimerHumeur() {
        try {
            System.out.println("\n--- Supprimer une Humeur ---");
            System.out.print("ID de l'humeur à supprimer: ");
            int idDelete = sc.nextInt();
            sc.nextLine();

            humeur humeurToDelete = humeurService.read(idDelete);
            if (humeurToDelete == null) {
                System.out.println(" Humeur non trouvée.");
                return;
            }

            System.out.print("Confirmer suppression? (o/n): ");
            String confirm = sc.nextLine();

            if (confirm.equalsIgnoreCase("o") || confirm.equalsIgnoreCase("oui")) {
                humeurService.delete(idDelete);
                System.out.println(" Humeur supprimée avec succès!");
            } else {
                System.out.println(" Suppression annulée.");
            }
        } catch (Exception e) {
            System.out.println(" Erreur: " + e.getMessage());
        }
    }

    public static void rechercherHumeurParId() {
        try {
            System.out.println("\n=== RECHERCHER UNE HUMEUR ===");
            System.out.print("ID de l'humeur à rechercher: ");
            int id = sc.nextInt();
            sc.nextLine();

            humeur h = humeurService.read(id);

            if (h == null) {
                System.out.println(" Aucune humeur trouvée avec l'ID " + id);
                return;
            }

            System.out.println("\n=== HUMEUR TROUVÉE ===");
            System.out.println("ID: " + h.getIdH());
            System.out.println("Date: " + h.getDate());
            System.out.println("Type: " + h.getTypeHumeur());
            System.out.println("Intensité: " + h.getIntensite() + "/10");
            System.out.println("User ID: " + h.getIdU());
            System.out.println("======================");

        } catch (Exception e) {
            System.out.println(" Erreur lors de la recherche: " + e.getMessage());
        }
    }

    public static void menuJournal() {
        int choix;
        do {
            System.out.println("\n===== MENU JOURNAL EMOTIONNEL =====");
            System.out.println("1. Ajouter une note");
            System.out.println("2. Afficher toutes les notes");
            System.out.println("3. Modifier une note");
            System.out.println("4. Supprimer une note");
            System.out.println("5. Rechercher une note par ID");
            System.out.println("0. Retour au menu principal");
            System.out.print("Votre choix: ");

            try {
                choix = sc.nextInt();
                sc.nextLine();

                switch (choix) {
                    case 1:
                        ajouterNoteJournal();
                        break;
                    case 2:
                        afficherNotes();
                        break;
                    case 3:
                        modifierNoteJournal();
                        break;
                    case 4:
                        supprimerNoteJournal();
                        break;
                    case 5:
                        rechercherJournalParId();
                        break;
                }
            } catch (Exception e) {
                System.out.println(" Erreur: " + e.getMessage());
                sc.nextLine();
                choix = -1;
            }
        } while (choix != 0);
    }

    public static void ajouterNoteJournal() {
        try {
            System.out.println("\n--- Ajouter une Note au Journal ---");

            System.out.print("Note personnelle: ");
            String notePersonnelle = sc.nextLine();

            JournalEmotionnel note = new JournalEmotionnel(notePersonnelle, LocalDate.now(), DEFAULT_USER_ID);
            journalService.create(note);
            System.out.println(" Note ajoutée avec succès! ID: " + note.getIdJournal());

        } catch (Exception e) {
            System.out.println(" Erreur lors de l'ajout de la note: " + e.getMessage());
        }
    }

    public static void afficherNotes() {
        try {
            List<JournalEmotionnel> list = journalService.readAll();
            list.sort(Comparator.comparingInt(JournalEmotionnel::getIdJournal));

            System.out.println("\n=== Liste des Notes (" + list.size() + ") ===");
            if (list.isEmpty()) {
                System.out.println("Aucune note enregistrée.");
            } else {
                for (JournalEmotionnel note : list) {
                    System.out.println("ID: " + note.getIdJournal() +
                            " | Date: " + note.getDateCreation() +
                            " | Note: " + note.getNotePersonnelle() +
                            " | User ID: " + note.getIdU());
                }
            }
        } catch (Exception e) {
            System.out.println(" Erreur lors de la récupération des notes: " + e.getMessage());
        }
    }

    public static void modifierNoteJournal() {
        try {
            System.out.println("\n=== MODIFIER UNE NOTE DU JOURNAL ===");
            System.out.print("ID de la note à modifier: ");
            int idUpdate = sc.nextInt();
            sc.nextLine();

            JournalEmotionnel noteToUpdate = journalService.read(idUpdate);
            if (noteToUpdate == null) {
                System.out.println(" Note non trouvée.");
                return;
            }

            System.out.println("\n Note actuelle:");
            System.out.println("   Note: " + noteToUpdate.getNotePersonnelle());

            System.out.print("\nNouvelle note: ");
            String newNote = sc.nextLine();

            noteToUpdate.setNotePersonnelle(newNote);
            noteToUpdate.setDateCreation(LocalDate.now());

            journalService.update(noteToUpdate);
            System.out.println("\n Note modifiée avec succès!");

        } catch (Exception e) {
            System.out.println(" Erreur: " + e.getMessage());
        }
    }

    public static void supprimerNoteJournal() {
        try {
            System.out.println("\n--- Supprimer une Note du Journal ---");
            System.out.print("ID de la note à supprimer: ");
            int idDelete = sc.nextInt();
            sc.nextLine();

            JournalEmotionnel noteToDelete = journalService.read(idDelete);
            if (noteToDelete == null) {
                System.out.println(" Note non trouvée.");
                return;
            }

            System.out.print("Confirmer suppression? (o/n): ");
            String confirm = sc.nextLine();

            if (confirm.equalsIgnoreCase("o") || confirm.equalsIgnoreCase("oui")) {
                journalService.delete(idDelete);
                System.out.println(" Note supprimée avec succès!");
            } else {
                System.out.println(" Suppression annulée.");
            }
        } catch (Exception e) {
            System.out.println(" Erreur: " + e.getMessage());
        }
    }

    public static void rechercherJournalParId() {
        try {
            System.out.println("\n=== RECHERCHER UNE NOTE DU JOURNAL ===");
            System.out.print("ID de la note à rechercher: ");
            int id = sc.nextInt();
            sc.nextLine();

            JournalEmotionnel note = journalService.read(id);

            if (note == null) {
                System.out.println(" Aucune note trouvée avec l'ID " + id);
                return;
            }

            System.out.println("\n=== NOTE TROUVÉE ===");
            System.out.println("ID: " + note.getIdJournal());
            System.out.println("Date: " + note.getDateCreation());
            System.out.println("Note: " + note.getNotePersonnelle());
            System.out.println("User ID: " + note.getIdU());
            System.out.println("======================");

        } catch (Exception e) {
            System.out.println(" Erreur lors de la recherche: " + e.getMessage());
        }
    }

    public static void menuAnalyseJournaux() {
        // Code from Main.menuAnalyseJournaux()
    }
}
