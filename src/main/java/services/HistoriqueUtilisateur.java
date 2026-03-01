package services;

import entities.Session;

import java.util.List;

class HistoriqueUtilisateur {
    private List<Session> sessions;

    public int getTotalSessions() {
        return sessions.size();
    }

    public boolean aScoreParfait() { /* implémentation */
        return false;
    }

    public int getNbSessionsBienEtreMax() { /* implémentation */
        return 0;
    }

    public boolean estRegulierDepuis(int jours) { /* implémentation */
        return false;
    }

    public int getNbTypesExercices() { /* implémentation */
        return 0;
    }

    public int getNbExercicesDifferents() { /* implémentation */
        return 0;
    }
}
