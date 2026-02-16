/* ============================================================
   MindTrack - BASE COMPLETE (ancienne + nouvelles entités)
   Compatible: MariaDB 10.4+ / MySQL 5.7+
   - Tables de base (vos tables)
   - Relations (FK)
   - Nouvelles entités créatives (évolutives)
   - Vues (jointures) prêtes
   ============================================================ */

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

DROP DATABASE IF EXISTS mindtrack;
CREATE DATABASE mindtrack
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE mindtrack;

-- ============================================================
-- 1) VOS TABLES (base)
-- ============================================================

CREATE TABLE utilisateur (
  idU INT(11) NOT NULL AUTO_INCREMENT,
  nomU VARCHAR(255) NOT NULL,
  prenomU VARCHAR(255) NOT NULL,
  emailU VARCHAR(255) NOT NULL,
  mdpsU VARCHAR(255) NOT NULL,
  ageU INT(11) NOT NULL,
  PRIMARY KEY (idU),
  UNIQUE KEY uk_utilisateur_email (emailU)
) ENGINE=InnoDB;

CREATE TABLE profilpsychologique (
  idP INT(11) NOT NULL AUTO_INCREMENT,
  NiveauStress INT(11) NOT NULL,
  NiveauMotivation INT(11) NOT NULL,
  Description VARCHAR(255) DEFAULT NULL,
  idU INT(11) NOT NULL,
  PRIMARY KEY (idP),
  UNIQUE KEY uk_profil_user (idU),
  CONSTRAINT fk_profil_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE humeur (
  idH INT(11) NOT NULL AUTO_INCREMENT,
  date DATE NOT NULL,
  TypeHumeur VARCHAR(255) NOT NULL,
  intensite INT(11) NOT NULL,
  idU INT(11) NOT NULL,
  PRIMARY KEY (idH),
  KEY idx_humeur_user (idU),
  KEY idx_humeur_date (date),
  CONSTRAINT fk_humeur_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE journalemotionnel (
  idJ INT(11) NOT NULL AUTO_INCREMENT,
  NotePersonnelle VARCHAR(255) NOT NULL,
  dateCreation DATE NOT NULL,
  idU INT(11) NOT NULL,
  PRIMARY KEY (idJ),
  KEY idx_journal_user (idU),
  KEY idx_journal_date (dateCreation),
  CONSTRAINT fk_journal_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE objectif (
  idObj INT(11) NOT NULL AUTO_INCREMENT,
  titre VARCHAR(255) NOT NULL,
  descriprion VARCHAR(255) DEFAULT NULL,
  dateDebut DATE NOT NULL,
  dateFin DATE NOT NULL,
  statut VARCHAR(255) NOT NULL,
  idU INT(11) NOT NULL,
  PRIMARY KEY (idObj),
  KEY idx_objectif_user (idU),
  KEY idx_objectif_dates (dateDebut, dateFin),
  CONSTRAINT fk_objectif_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE planaction (
  idPlan INT(11) NOT NULL AUTO_INCREMENT,
  etape VARCHAR(255) NOT NULL,
  priorite INT(11) NOT NULL,
  idObj INT(11) NOT NULL,
  PRIMARY KEY (idPlan),
  KEY idx_plan_objectif (idObj),
  CONSTRAINT fk_plan_objectif
    FOREIGN KEY (idObj) REFERENCES objectif(idObj)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE habitude (
  idHabitude INT(11) NOT NULL AUTO_INCREMENT,
  nom VARCHAR(255) NOT NULL,
  frequence VARCHAR(255) NOT NULL,
  objectif VARCHAR(255) NOT NULL,
  idU INT(11) NOT NULL,
  PRIMARY KEY (idHabitude),
  KEY idx_habitude_user (idU),
  CONSTRAINT fk_habitude_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE suivihabitude (
  idSuivi INT(11) NOT NULL AUTO_INCREMENT,
  date DATE NOT NULL,
  etat TINYINT(1) NOT NULL,
  idHabitude INT(11) NOT NULL,
  PRIMARY KEY (idSuivi),
  UNIQUE KEY uk_suivi_unique_day (idHabitude, date),
  KEY idx_suivi_habitude (idHabitude),
  CONSTRAINT fk_suivi_habitude
    FOREIGN KEY (idHabitude) REFERENCES habitude(idHabitude)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE exercice (
  idEx INT(11) NOT NULL AUTO_INCREMENT,
  nom VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  duree INT(11) NOT NULL,
  PRIMARY KEY (idEx)
) ENGINE=InnoDB;

CREATE TABLE sessionexercice (
  idSession INT(11) NOT NULL AUTO_INCREMENT,
  dateSession DATE NOT NULL,
  Resultat VARCHAR(255) NOT NULL,
  idU INT(11) NOT NULL,
  idEx INT(11) NOT NULL,
  PRIMARY KEY (idSession),
  KEY idx_session_user (idU),
  KEY idx_session_ex (idEx),
  KEY idx_session_date (dateSession),
  CONSTRAINT fk_session_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_session_exercice
    FOREIGN KEY (idEx) REFERENCES exercice(idEx)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 2) NOUVELLES ENTITES CREATIVES (évolutives)
-- ============================================================

-- (A) SécuritéSession : multi-sessions, IP, device, 2FA...
CREATE TABLE securitesession (
  idSecurite INT AUTO_INCREMENT PRIMARY KEY,
  idU INT NOT NULL,
  jetonSession VARCHAR(255) NOT NULL,
  empreinteAppareil VARCHAR(255) NULL,
  adresseIP VARCHAR(64) NULL,
  derniereConnexion DATETIME NULL,
  tentativesEchouees INT NOT NULL DEFAULT 0,
  doubleAuthentification TINYINT(1) NOT NULL DEFAULT 0,
  actif TINYINT(1) NOT NULL DEFAULT 1,
  KEY idx_sec_user (idU),
  UNIQUE KEY uk_sec_token (jetonSession),
  CONSTRAINT fk_sec_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (B) CoffreConfidentialite : consentement, chiffrement (RGPD)
CREATE TABLE coffreconfidentialite (
  idCoffre INT AUTO_INCREMENT PRIMARY KEY,
  idU INT NOT NULL,
  modeChiffrement VARCHAR(50) NOT NULL DEFAULT 'AES',
  consentement TINYINT(1) NOT NULL DEFAULT 0,
  dateConsentement DATE NULL,
  UNIQUE KEY uk_coffre_user (idU),
  CONSTRAINT fk_coffre_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (C) IndiceBienEtre : score global calculé dans le temps
CREATE TABLE indicebienetre (
  idIndice INT AUTO_INCREMENT PRIMARY KEY,
  idU INT NOT NULL,
  scoreGlobal INT NOT NULL,
  scoreStress INT NOT NULL,
  scoreMotivation INT NOT NULL,
  niveauRisque VARCHAR(50) NOT NULL,
  dateCalcul DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_indice_user (idU),
  KEY idx_indice_date (dateCalcul),
  CONSTRAINT fk_indice_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (D) TendanceEmotionnelle : tendances / anomalies sur période
CREATE TABLE tendanceemotionnelle (
  idTendance INT AUTO_INCREMENT PRIMARY KEY,
  idU INT NOT NULL,
  periode VARCHAR(20) NOT NULL, -- jour/semaine/mois
  debut DATE NOT NULL,
  fin DATE NOT NULL,
  humeurDominante VARCHAR(50) NULL,
  intensiteMoyenne DOUBLE NULL,
  variabilite DOUBLE NULL,
  anomalieDetectee TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_tendance_user (idU),
  KEY idx_tendance_range (debut, fin),
  CONSTRAINT fk_tendance_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (E) AnalyseReflexive : analyse d’une note de journal (keywords, thème…)
CREATE TABLE analysereflexive (
  idAnalyse INT AUTO_INCREMENT PRIMARY KEY,
  idJ INT NOT NULL,
  motsCles VARCHAR(255) NULL,
  themeDominant VARCHAR(100) NULL,
  sentimentGlobal VARCHAR(50) NULL,
  dateAnalyse DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_analyse_journal (idJ),
  CONSTRAINT fk_analyse_journal
    FOREIGN KEY (idJ) REFERENCES journalemotionnel(idJ)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (F) JalonProgression : milestones d’un objectif
CREATE TABLE jalonprogression (
  idJalon INT AUTO_INCREMENT PRIMARY KEY,
  idObj INT NOT NULL,
  titre VARCHAR(150) NOT NULL,
  dateCible DATE NOT NULL,
  atteint TINYINT(1) NOT NULL DEFAULT 0,
  dateAtteinte DATE NULL,
  pourcentageProgression INT NOT NULL DEFAULT 0,
  KEY idx_jalon_obj (idObj),
  CONSTRAINT fk_jalon_obj
    FOREIGN KEY (idObj) REFERENCES objectif(idObj)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (G) PlanificateurIntelligent : planification auto par objectif
CREATE TABLE planificateurintelligent (
  idPlanificateur INT AUTO_INCREMENT PRIMARY KEY,
  idObj INT NOT NULL,
  modeOrganisation VARCHAR(30) NOT NULL DEFAULT 'priorite',
  capaciteQuotidienne INT NOT NULL DEFAULT 3,
  derniereGeneration DATETIME NULL,
  UNIQUE KEY uk_planif_obj (idObj),
  CONSTRAINT fk_planif_obj
    FOREIGN KEY (idObj) REFERENCES objectif(idObj)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (H) RoutineAdaptative : routines personnalisées (composées d’exercices)
CREATE TABLE routineadaptative (
  idRoutine INT AUTO_INCREMENT PRIMARY KEY,
  idU INT NOT NULL,
  nomRoutine VARCHAR(150) NOT NULL,
  etatCible VARCHAR(50) NOT NULL, -- calme/focus/motivation
  dureeTotale INT NOT NULL,
  difficulte VARCHAR(30) NOT NULL DEFAULT 'facile',
  creeLe DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_routine_user (idU),
  CONSTRAINT fk_routine_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Relation N-N routine <-> exercice
CREATE TABLE routine_exercice (
  idRoutine INT NOT NULL,
  idEx INT NOT NULL,
  ordre INT NOT NULL DEFAULT 1,
  PRIMARY KEY (idRoutine, idEx),
  KEY idx_rex_ex (idEx),
  CONSTRAINT fk_rex_routine
    FOREIGN KEY (idRoutine) REFERENCES routineadaptative(idRoutine)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_rex_ex
    FOREIGN KEY (idEx) REFERENCES exercice(idEx)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (I) CartePerformance : score d’une session d’exercice
CREATE TABLE carteperformance (
  idCarte INT AUTO_INCREMENT PRIMARY KEY,
  idSession INT NOT NULL,
  scoreConcentration INT NOT NULL DEFAULT 0,
  scoreCalme INT NOT NULL DEFAULT 0,
  scoreFatigue INT NOT NULL DEFAULT 0,
  commentaire VARCHAR(255) NULL,
  UNIQUE KEY uk_carte_session (idSession),
  CONSTRAINT fk_carte_session
    FOREIGN KEY (idSession) REFERENCES sessionexercice(idSession)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (J) MoteurDeSerie : streaks / gamification par habitude
CREATE TABLE moteurdserie (
  idMoteur INT AUTO_INCREMENT PRIMARY KEY,
  idHabitude INT NOT NULL,
  serieActuelle INT NOT NULL DEFAULT 0,
  meilleureSerie INT NOT NULL DEFAULT 0,
  tauxReussite DOUBLE NOT NULL DEFAULT 0,
  niveauDifficulte VARCHAR(30) NOT NULL DEFAULT 'normal',
  UNIQUE KEY uk_moteur_hab (idHabitude),
  CONSTRAINT fk_moteur_hab
    FOREIGN KEY (idHabitude) REFERENCES habitude(idHabitude)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- (K) PolitiqueContenu : règles de modération (admin)
CREATE TABLE politiquecontenu (
  idPolitique INT AUTO_INCREMENT PRIMARY KEY,
  categorie VARCHAR(50) NOT NULL,
  niveauGravite VARCHAR(20) NOT NULL,
  action VARCHAR(50) NOT NULL,
  dateCreation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Signalements (optionnel mais utile pour avancer)
CREATE TABLE signalement (
  idSignalement INT AUTO_INCREMENT PRIMARY KEY,
  idPolitique INT NULL,
  idU INT NULL, -- qui signale / ou admin
  typeEntite VARCHAR(50) NOT NULL, -- journalemotionnel / objectif / etc
  idEntite INT NOT NULL,
  raison VARCHAR(255) NOT NULL,
  statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
  dateSignalement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_sig_user (idU),
  KEY idx_sig_type (typeEntite, idEntite),
  CONSTRAINT fk_sig_politique
    FOREIGN KEY (idPolitique) REFERENCES politiquecontenu(idPolitique)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_sig_user
    FOREIGN KEY (idU) REFERENCES utilisateur(idU)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 3) VUES (JOINTURES) POUR TRAVAILLER FACILEMENT
-- ============================================================

-- Vue 1 : Base complète (ancienne)
DROP VIEW IF EXISTS v_mindtrack_full;
CREATE VIEW v_mindtrack_full AS
SELECT
  u.idU, u.nomU, u.prenomU, u.emailU, u.ageU,

  pp.idP, pp.NiveauStress, pp.NiveauMotivation, pp.Description AS profilDescription,

  h.idH, h.date AS humeurDate, h.TypeHumeur, h.intensite,

  j.idJ, j.dateCreation, j.NotePersonnelle,

  o.idObj, o.titre, o.descriprion, o.dateDebut, o.dateFin, o.statut,

  pa.idPlan, pa.etape, pa.priorite,

  hab.idHabitude, hab.nom AS habitudeNom, hab.frequence, hab.objectif AS objectifHabitude,

  sh.idSuivi, sh.date AS suiviDate, sh.etat,

  se.idSession, se.dateSession, se.Resultat,

  ex.idEx, ex.nom AS exerciceNom, ex.type AS exerciceType, ex.duree

FROM utilisateur u
LEFT JOIN profilpsychologique pp ON pp.idU = u.idU
LEFT JOIN humeur h               ON h.idU = u.idU
LEFT JOIN journalemotionnel j    ON j.idU = u.idU
LEFT JOIN objectif o             ON o.idU = u.idU
LEFT JOIN planaction pa          ON pa.idObj = o.idObj
LEFT JOIN habitude hab           ON hab.idU = u.idU
LEFT JOIN suivihabitude sh       ON sh.idHabitude = hab.idHabitude
LEFT JOIN sessionexercice se     ON se.idU = u.idU
LEFT JOIN exercice ex            ON ex.idEx = se.idEx;

-- Vue 2 : Sécurité + confidentialité
DROP VIEW IF EXISTS v_mindtrack_security;
CREATE VIEW v_mindtrack_security AS
SELECT
  u.idU, u.emailU,
  cs.idCoffre, cs.modeChiffrement, cs.consentement, cs.dateConsentement,
  ss.idSecurite, ss.jetonSession, ss.empreinteAppareil, ss.adresseIP,
  ss.derniereConnexion, ss.tentativesEchouees, ss.doubleAuthentification, ss.actif
FROM utilisateur u
LEFT JOIN coffreconfidentialite cs ON cs.idU = u.idU
LEFT JOIN securitesession ss       ON ss.idU = u.idU;

-- Vue 3 : Analytics & futur (scores / tendances / insights)
DROP VIEW IF EXISTS v_mindtrack_analytics;
CREATE VIEW v_mindtrack_analytics AS
SELECT
  u.idU, u.nomU, u.prenomU,

  ib.idIndice, ib.scoreGlobal, ib.scoreStress, ib.scoreMotivation, ib.niveauRisque, ib.dateCalcul,

  te.idTendance, te.periode, te.debut, te.fin, te.humeurDominante, te.intensiteMoyenne, te.variabilite, te.anomalieDetectee,

  ar.idAnalyse, ar.motsCles, ar.themeDominant, ar.sentimentGlobal, ar.dateAnalyse,
  j.idJ, j.dateCreation, j.NotePersonnelle

FROM utilisateur u
LEFT JOIN indicebienetre ib       ON ib.idU = u.idU
LEFT JOIN tendanceemotionnelle te ON te.idU = u.idU
LEFT JOIN journalemotionnel j     ON j.idU = u.idU
LEFT JOIN analysereflexive ar     ON ar.idJ = j.idJ;

-- Vue 4 : Objectifs avancés (jalons + planif)
DROP VIEW IF EXISTS v_objectifs_avances;
CREATE VIEW v_objectifs_avances AS
SELECT
  o.idObj, o.titre, o.statut, o.dateDebut, o.dateFin, o.idU,
  jp.idJalon, jp.titre AS jalonTitre, jp.dateCible, jp.atteint, jp.dateAtteinte, jp.pourcentageProgression,
  pi.idPlanificateur, pi.modeOrganisation, pi.capaciteQuotidienne, pi.derniereGeneration
FROM objectif o
LEFT JOIN jalonprogression jp       ON jp.idObj = o.idObj
LEFT JOIN planificateurintelligent pi ON pi.idObj = o.idObj;

-- Vue 5 : Habitudes avancées (streak engine)
DROP VIEW IF EXISTS v_habitudes_avancees;
CREATE VIEW v_habitudes_avancees AS
SELECT
  hab.idHabitude, hab.nom, hab.frequence, hab.objectif, hab.idU,
  ms.idMoteur, ms.serieActuelle, ms.meilleureSerie, ms.tauxReussite, ms.niveauDifficulte,
  sh.idSuivi, sh.date AS suiviDate, sh.etat
FROM habitude hab
LEFT JOIN moteurdserie ms ON ms.idHabitude = hab.idHabitude
LEFT JOIN suivihabitude sh ON sh.idHabitude = hab.idHabitude;

-- Vue 6 : Exercices avancés (routine + scorecard)
DROP VIEW IF EXISTS v_exercices_avances;
CREATE VIEW v_exercices_avances AS
SELECT
  u.idU, u.nomU, u.prenomU,

  r.idRoutine, r.nomRoutine, r.etatCible, r.dureeTotale, r.difficulte, r.creeLe,

  ex.idEx, ex.nom AS exerciceNom, ex.type AS exerciceType, ex.duree AS dureeExercice,
  rex.ordre,

  se.idSession, se.dateSession, se.Resultat,
  cp.idCarte, cp.scoreConcentration, cp.scoreCalme, cp.scoreFatigue, cp.commentaire

FROM utilisateur u
LEFT JOIN routineadaptative r     ON r.idU = u.idU
LEFT JOIN routine_exercice rex    ON rex.idRoutine = r.idRoutine
LEFT JOIN exercice ex             ON ex.idEx = rex.idEx
LEFT JOIN sessionexercice se      ON se.idU = u.idU AND se.idEx = ex.idEx
LEFT JOIN carteperformance cp     ON cp.idSession = se.idSession;

COMMIT;

/* ============================================================
   UTILISATION RAPIDE :
   - Données anciennes:   SELECT * FROM v_mindtrack_full;
   - Sécurité:            SELECT * FROM v_mindtrack_security;
   - Analytics:           SELECT * FROM v_mindtrack_analytics;
   - Objectifs avancés:   SELECT * FROM v_objectifs_avances;
   - Habitudes avancées:  SELECT * FROM v_habitudes_avancees;
   - Exercices avancés:   SELECT * FROM v_exercices_avances;
   ============================================================ */
