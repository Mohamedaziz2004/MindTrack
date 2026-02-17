package org.mindtrack.mindtrackfxx.controller;

import entities.humeur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import servives.humeurService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsController {

    @FXML private Label totalEntriesLabel;
    @FXML private Label happyCountLabel;
    @FXML private Label happyPercentLabel;
    @FXML private Label happyAvgIntensityLabel;
    @FXML private Label calmCountLabel;
    @FXML private Label calmPercentLabel;
    @FXML private Label calmAvgIntensityLabel;
    @FXML private Label neutralCountLabel;
    @FXML private Label neutralPercentLabel;
    @FXML private Label neutralAvgIntensityLabel;
    @FXML private Label sadCountLabel;
    @FXML private Label sadPercentLabel;
    @FXML private Label sadAvgIntensityLabel;
    @FXML private Label anxiousCountLabel;
    @FXML private Label anxiousPercentLabel;
    @FXML private Label anxiousAvgIntensityLabel;
    @FXML private Label insightLabel;

    @FXML private PieChart moodPieChart;
    @FXML private BarChart<String, Number> intensityBarChart;

    private final humeurService moodService = new humeurService();

    @FXML
    private void initialize() {
        calculateStatistics();
    }

    private void calculateStatistics() {
        List<humeur> allMoods = moodService.readAll();
        int total = allMoods.size();

        if (total == 0) {
            totalEntriesLabel.setText("Total: 0 entries");
            insightLabel.setText("No mood data available. Start tracking your moods!");
            return;
        }

        totalEntriesLabel.setText("Total: " + total + " entries");

        // Count moods and calculate intensities
        Map<String, Integer> moodCounts = new HashMap<>();
        Map<String, Integer> moodIntensitySums = new HashMap<>();

        String[] moodTypes = {"Happy", "Calm", "Neutral", "Sad", "Anxious"};
        for (String mood : moodTypes) {
            moodCounts.put(mood, 0);
            moodIntensitySums.put(mood, 0);
        }

        for (humeur m : allMoods) {
            String type = m.getTypeHumeur();
            // Normalize mood type
            for (String mood : moodTypes) {
                if (type.toLowerCase().contains(mood.toLowerCase())) {
                    moodCounts.put(mood, moodCounts.get(mood) + 1);
                    moodIntensitySums.put(mood, moodIntensitySums.get(mood) + m.getIntensite());
                    break;
                }
            }
        }

        // Update labels
        updateMoodCard(happyCountLabel, happyPercentLabel, happyAvgIntensityLabel,
                moodCounts.get("Happy"), moodIntensitySums.get("Happy"), total);
        updateMoodCard(calmCountLabel, calmPercentLabel, calmAvgIntensityLabel,
                moodCounts.get("Calm"), moodIntensitySums.get("Calm"), total);
        updateMoodCard(neutralCountLabel, neutralPercentLabel, neutralAvgIntensityLabel,
                moodCounts.get("Neutral"), moodIntensitySums.get("Neutral"), total);
        updateMoodCard(sadCountLabel, sadPercentLabel, sadAvgIntensityLabel,
                moodCounts.get("Sad"), moodIntensitySums.get("Sad"), total);
        updateMoodCard(anxiousCountLabel, anxiousPercentLabel, anxiousAvgIntensityLabel,
                moodCounts.get("Anxious"), moodIntensitySums.get("Anxious"), total);

        // Create Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (String mood : moodTypes) {
            int count = moodCounts.get(mood);
            if (count > 0) {
                pieData.add(new PieChart.Data(mood + " (" + count + ")", count));
            }
        }
        moodPieChart.setData(pieData);

        // Apply colors to pie chart
        applyPieChartColors();

        // Create Bar Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Intensity");
        for (String mood : moodTypes) {
            int count = moodCounts.get(mood);
            double avgIntensity = count > 0 ? (double) moodIntensitySums.get(mood) / count : 0;
            series.getData().add(new XYChart.Data<>(mood, avgIntensity));
        }
        intensityBarChart.getData().clear();
        intensityBarChart.getData().add(series);

        // Find most common mood
        String mostCommonMood = "None";
        int maxCount = 0;
        for (String mood : moodTypes) {
            if (moodCounts.get(mood) > maxCount) {
                maxCount = moodCounts.get(mood);
                mostCommonMood = mood;
            }
        }

        // Calculate overall average intensity
        double overallAvgIntensity = allMoods.stream()
                .mapToInt(humeur::getIntensite)
                .average()
                .orElse(0);

        insightLabel.setText(String.format(
                "Your most common mood is %s (%d times) • Average intensity: %.1f/10",
                mostCommonMood, maxCount, overallAvgIntensity));
    }

    private void updateMoodCard(Label countLabel, Label percentLabel, Label intensityLabel,
                                 int count, int intensitySum, int total) {
        countLabel.setText(String.valueOf(count));
        double percent = total > 0 ? (count * 100.0 / total) : 0;
        percentLabel.setText(String.format("%.1f%%", percent));
        double avgIntensity = count > 0 ? (double) intensitySum / count : 0;
        intensityLabel.setText(String.format("Avg: %.1f", avgIntensity));
    }

    private void applyPieChartColors() {
        // Apply custom colors after data is set
        int index = 0;
        for (PieChart.Data data : moodPieChart.getData()) {
            String color;
            String name = data.getName().toLowerCase();
            if (name.contains("happy")) {
                color = "#22c55e";
            } else if (name.contains("calm")) {
                color = "#0ea5e9";
            } else if (name.contains("neutral")) {
                color = "#64748b";
            } else if (name.contains("sad")) {
                color = "#f59e0b";
            } else if (name.contains("anxious")) {
                color = "#ef4444";
            } else {
                color = "#94a3b8";
            }
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            index++;
        }
    }
}
