package services;

import entities.Exercice;
import dao.ExerciceDAO;
import utils.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ExerciceService implements IService<Exercice, Integer> {

    private final ExerciceDAO exerciceDAO;

    public ExerciceService() {
        this.exerciceDAO = new ExerciceDAO();
    }

    @Override
    public Exercice ajouter(Exercice exercice) throws SQLException {
        if (!valider(exercice)) {
            throw new IllegalArgumentException("Les données de l'exercice sont invalides");
        }

        // Set creation date if not already set
        if (exercice.getDateCreation() == null) {
            exercice.setDateCreation(LocalDateTime.now());
        }

        System.out.println("=== AJOUT EXERCICE ===");
        System.out.println("Nom: " + exercice.getNom());
        System.out.println("Type: " + exercice.getType());
        System.out.println("Durée: " + exercice.getDuree());
        System.out.println("Difficulté: " + exercice.getDifficulte());
        System.out.println("Description: " + exercice.getDescription());
        System.out.println("Démarche: " + exercice.getDemarche());
        System.out.println("Date création: " + exercice.getDateCreation());
        System.out.println("======================");

        return exerciceDAO.create(exercice);
    }

    @Override
    public Exercice getById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return exerciceDAO.read(id);
    }

    @Override
    public List<Exercice> getAll() throws SQLException {
        return exerciceDAO.findAll();
    }

    @Override
    public boolean modifier(Exercice exercice) throws SQLException {
        if (exercice == null || exercice.getIdExercice() <= 0) {
            throw new IllegalArgumentException("Exercice invalide ou ID manquant");
        }
        if (!valider(exercice)) {
            throw new IllegalArgumentException("Les données de l'exercice sont invalides");
        }
        exercice.setDateModification(LocalDateTime.now());
        return exerciceDAO.update(exercice);
    }

    @Override
    public boolean supprimer(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return exerciceDAO.delete(id);
    }

    @Override
    public boolean valider(Exercice exercice) {
        if (exercice == null) return false;

        // Validation avec ValidationUtils
        if (!ValidationUtils.isValidName(exercice.getNom())) {
            System.err.println("Erreur: Nom invalide");
            return false;
        }

        if (!ValidationUtils.isValidExerciceType(exercice.getType())) {
            System.err.println("Erreur: Type invalide");
            return false;
        }

        if (!ValidationUtils.isPositiveInteger(exercice.getDuree())) {
            System.err.println("Erreur: Durée doit être positive");
            return false;
        }
        if (exercice.getDuree() > 480) {
            System.err.println("Erreur: Durée max 480 minutes");
            return false;
        }

        if (!ValidationUtils.isValidDifficulte(exercice.getDifficulte())) {
            System.err.println("Erreur: Difficulté invalide");
            return false;
        }

        if (!ValidationUtils.isValidDescription(exercice.getDescription())) {
            System.err.println("Erreur: Description trop longue");
            return false;
        }

        // La démarche est optionnelle, pas de validation stricte
        if (exercice.getDemarche() != null && exercice.getDemarche().length() > 2000) {
            System.err.println("Erreur: Démarche trop longue (max 2000 caractères)");
            return false;
        }

        return true;
    }

    // Business methods
    public List<Exercice> rechercherParNom(String nom) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            return getAll();
        }
        return exerciceDAO.searchByNom(nom);
    }

    public List<Exercice> rechercherParType(String type) throws SQLException {
        if (type == null || type.trim().isEmpty()) {
            return getAll();
        }
        return exerciceDAO.findByType(type);
    }

    public List<Exercice> rechercherParDifficulte(String difficulte) throws SQLException {
        if (difficulte == null || difficulte.trim().isEmpty()) {
            return getAll();
        }
        return exerciceDAO.findByDifficulte(difficulte);
    }

    public boolean peutEtreSupprime(int exerciceId) throws SQLException {
        Exercice exercice = getById(exerciceId);
        if (exercice == null) return false;
        return exercice.getSessions().isEmpty();
    }

    public Exercice dupliquerExercice(int exerciceId, String nouveauNom) throws SQLException {
        Exercice original = getById(exerciceId);
        if (original == null) {
            throw new SQLException("Exercice original non trouvé");
        }

        Exercice copie = new Exercice();
        copie.setNom(nouveauNom != null ? nouveauNom : original.getNom() + " (copie)");
        copie.setType(original.getType());
        copie.setDuree(original.getDuree());
        copie.setDifficulte(original.getDifficulte());
        copie.setDescription(original.getDescription());
        copie.setDemarche(original.getDemarche()); // Important: copier aussi la démarche

        return ajouter(copie);
    }
}