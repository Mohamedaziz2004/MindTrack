package services;

import entities.Session;
import entities.Exercice;
import entities.Progression;
import dao.SessionDAO;
import dao.ExerciceDAO;
import dao.ProgressionDAO;
import utils.SessionManager;
import utils.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

public class SessionService implements IService<Session, Integer> {

    private final SessionDAO sessionDAO;
    private final ExerciceDAO exerciceDAO;
    private final ProgressionDAO progressionDAO;
    private final SessionManager sessionManager;

    public SessionService() {
        this.sessionDAO = new SessionDAO();
        this.exerciceDAO = new ExerciceDAO();
        this.progressionDAO = new ProgressionDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    @Override
    public Session ajouter(Session session) throws SQLException {
        if (!valider(session)) {
            throw new IllegalArgumentException("Les données de la session sont invalides");
        }

        if (session.getDateSession() == null) {
            session.setDateSession(java.time.LocalDate.now());
        }

        // Ensure user ID is set
        if (session.getIdUser() <= 0) {
            session.setIdUser(sessionManager.getCurrentUserId());
        }

        // Ensure exercice ID is set
        if (session.getExercice() != null && session.getIdExercice() <= 0) {
            session.setIdExercice(session.getExercice().getIdExercice());
        }

        return sessionDAO.create(session);
    }

    @Override
    public Session getById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return sessionDAO.read(id);
    }

    @Override
    public List<Session> getAll() throws SQLException {
        return sessionDAO.findAll();
    }

    @Override
    public boolean modifier(Session session) throws SQLException {
        if (session == null || session.getIdSession() <= 0) {
            throw new IllegalArgumentException("Session invalide ou ID manquant");
        }
        if (!valider(session)) {
            throw new IllegalArgumentException("Les données de la session sont invalides");
        }
        return sessionDAO.update(session);
    }

    @Override
    public boolean supprimer(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return sessionDAO.delete(id);
    }

    @Override
    public boolean valider(Session session) {
        if (session == null) return false;

        if (session.getExercice() == null && session.getIdExercice() <= 0) {
            System.err.println("Erreur: Une session doit être associée à un exercice");
            return false;
        }

        if (!ValidationUtils.isValidSessionResult(session.getResultat())) {
            System.err.println("Erreur: Résultat trop long");
            return false;
        }

        if (!ValidationUtils.isValidCommentaires(session.getCommentaires())) {
            System.err.println("Erreur: Commentaires trop longs");
            return false;
        }

        return true;
    }

    // Business methods
    public Session demarrerSession(Exercice exercice) throws SQLException {
        if (exercice == null || exercice.getIdExercice() <= 0) {
            throw new IllegalArgumentException("Exercice invalide");
        }

        Session session = new Session(exercice);
        session.demarrer();
        session.setIdUser(sessionManager.getCurrentUserId());

        return ajouter(session);
    }

    public Session terminerSession(int sessionId, String resultat, String commentaires) throws SQLException {
        Session session = getById(sessionId);
        if (session == null) {
            throw new SQLException("Session non trouvée");
        }

        if (session.isTerminee()) {
            throw new IllegalStateException("Cette session est déjà terminée");
        }

        session.terminer();
        session.enregistrerResultat(resultat, commentaires);

        modifier(session);

        return session;
    }

    public Session terminerSessionAvecProgression(int sessionId, String resultat, String commentaires,
                                                  Integer score, Integer ressenti) throws SQLException {
        Session session = terminerSession(sessionId, resultat, commentaires);

        // Créer une progression
        Progression progression = new Progression(sessionManager.getCurrentUserId(), session.getExercice());
        progression.setSession(session);
        progression.setScoreObtenu(score);
        progression.setRessentiUtilisateur(ressenti);
        progression.setNotesPersonnelles(commentaires);
        progression.setTempsPasse(session.getDureeReelle() != null ? session.getDureeReelle() : 0);

        progressionDAO.create(progression);
        session.setProgression(progression);

        return session;
    }

    public List<Session> getSessionsParExercice(int exerciceId) throws SQLException {
        return sessionDAO.findByExerciceId(exerciceId);
    }

    public List<Session> getSessionsParStatut(boolean terminee) throws SQLException {
        return sessionDAO.findByStatut(terminee);
    }

    public List<Session> getSessionsRecentes() throws SQLException {
        return sessionDAO.findRecentSessions();
    }

    public double getDureeMoyenneSessions(int exerciceId) throws SQLException {
        List<Session> sessions = getSessionsParExercice(exerciceId);
        if (sessions.isEmpty()) return 0;

        return sessions.stream()
                .filter(s -> s.getDureeReelle() != null && s.getDureeReelle() > 0)
                .mapToInt(Session::getDureeReelle)
                .average()
                .orElse(0);
    }

    public boolean peutEtreModifiee(int sessionId) throws SQLException {
        Session session = getById(sessionId);
        if (session == null) return false;
        return !session.isTerminee();
    }
}