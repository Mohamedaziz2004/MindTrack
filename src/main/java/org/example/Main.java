package org.example;

import org.example.controller.ObjectifController;
import org.example.controller.JalonProgressionController;
import org.example.controller.PlanActionController;
import org.example.model.Objectif;
import org.example.model.JalonProgression;
import org.example.model.PlanAction;

import java.time.LocalDate;
import java.util.List;

/**
 * MindTrack - Personal Objectives Management System
 * 
 * This application helps users manage their personal goals through:
 * 1. Objectif (Goals) - Main objectives with deadlines
 * 2. JalonProgression (Milestones) - Checkpoints to track progress
 * 3. PlanAction (Action Steps) - Specific actions with priorities to achieve goals
 * 4. PlanificateurIntelligent (Intelligent Planner) - Optimized planning and scheduling
 */
public class Main {

    public static void main(String[] args) {

        ObjectifController objectifController = new ObjectifController();
        JalonProgressionController jalonController = new JalonProgressionController();
        PlanActionController planActionController = new PlanActionController();

        System.out.println("=================================");
        System.out.println("🚀 MINDTRACK - OBJECTIVES SYSTEM");
        System.out.println("=================================\n");

        // ============================
        // CREATE OBJECTIFS (GOALS)
        // ============================
        System.out.println("📌 Creating Goals...");
        
        Objectif goal1 = new Objectif(
                0,
                "Master JavaScript Framework",
                "Learn JavaScript ES6+, React, and build modern web applications",
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                "Non commencée",
                1
        );

        Objectif goal2 = new Objectif(
                0,
                "Complete Cloud Infrastructure Project",
                "Design and deploy cloud infrastructure using Docker and Kubernetes",
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "En cours",
                1
        );

        objectifController.ajouterObjectif(goal1);
        objectifController.ajouterObjectif(goal2);
        System.out.println("✓ Goals created successfully!\n");

        // ============================
        // READ ALL OBJECTIFS
        // ============================
        System.out.println("📊 All Goals:");
        List<Objectif> objectifs = objectifController.getAllObjectifs();
        for (Objectif obj : objectifs) {
            System.out.println("  • " + obj.getTitre() + " (Status: " + obj.getStatut() + ")");
        }
        System.out.println();

        // ============================
        // CREATE MILESTONES
        // ============================
        System.out.println("🎯 Creating Milestones...");
        
        if (!objectifs.isEmpty()) {
            Objectif selectedGoal = objectifs.get(0);
            
            JalonProgression milestone1 = new JalonProgression(
                    0,
                    selectedGoal.getIdObj(),
                    "Complete JavaScript Basics",
                    LocalDate.now().plusWeeks(4),
                    false,
                    null,
                    0
            );

            JalonProgression milestone2 = new JalonProgression(
                    0,
                    selectedGoal.getIdObj(),
                    "Build First React App",
                    LocalDate.now().plusWeeks(8),
                    false,
                    null,
                    0
            );

            jalonController.ajouterJalon(milestone1);
            jalonController.ajouterJalon(milestone2);
            System.out.println("✓ Milestones created successfully!\n");

            // ============================
            // CREATE ACTION STEPS
            // ============================
            System.out.println("⚡ Creating Action Steps...");
            
            PlanAction action1 = new PlanAction(
                    0,
                    selectedGoal.getIdObj(),
                    "Study ES6 syntax and arrow functions",
                    1  // High priority
            );

            PlanAction action2 = new PlanAction(
                    0,
                    selectedGoal.getIdObj(),
                    "Complete JavaScript exercises on HackerRank",
                    1  // High priority
            );

            PlanAction action3 = new PlanAction(
                    0,
                    selectedGoal.getIdObj(),
                    "Install Node.js and npm",
                    2  // Medium priority
            );

            planActionController.ajouterPlanAction(action1);
            planActionController.ajouterPlanAction(action2);
            planActionController.ajouterPlanAction(action3);
            System.out.println("✓ Action steps created successfully!\n");

            // ============================
            // READ MILESTONES FOR GOAL
            // ============================
            System.out.println("📍 Milestones for " + selectedGoal.getTitre() + ":");
            List<JalonProgression> jalons = jalonController.getJalonsByObjectif(selectedGoal.getIdObj());
            for (JalonProgression jalon : jalons) {
                System.out.println("  • " + jalon.getTitre() + " (Target: " + jalon.getDateCible() + ")");
            }
            System.out.println();

            // ============================
            // READ ACTION STEPS FOR GOAL
            // ============================
            System.out.println("✓ Action Steps for " + selectedGoal.getTitre() + ":");
            List<PlanAction> actions = planActionController.getPlanActionsByObjectif(selectedGoal.getIdObj());
            for (PlanAction action : actions) {
                System.out.println("  • [" + action.getPrioriteLabel() + "] " + action.getEtape());
            }
            System.out.println();

            // ============================
            // UPDATE GOAL STATUS
            // ============================
            System.out.println("🔄 Updating Goal Status...");
            selectedGoal.setStatut("En cours");
            objectifController.modifierObjectif(selectedGoal);
            System.out.println("✓ Goal status updated to: " + selectedGoal.getStatut() + "\n");

            // ============================
            // MARK MILESTONE AS COMPLETE
            // ============================
            if (!jalons.isEmpty()) {
                System.out.println("✅ Marking First Milestone as Complete...");
                jalonController.completerJalon(jalons.get(0).getIdJalon());
                System.out.println("✓ Milestone marked as complete!\n");
            }
        }

        // ============================
        // STATISTICS
        // ============================
        System.out.println("=================================");
        System.out.println("📊 FINAL STATISTICS");
        System.out.println("=================================");
        System.out.println("Total Goals: " + objectifController.getAllObjectifs().size());
        System.out.println("Total Milestones: " + jalonController.getAllJalons().size());
        System.out.println("Total Action Steps: " + planActionController.getAllPlanActions().size());
        System.out.println("Active Goals: " + objectifController.getActiveObjectifs().size());
        System.out.println("\n✨ Application ready! Launch MainFx to use the GUI.\n");
    }
}
