package org.example.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {

    private static final String API_KEY = "AIzaSyDnC_Xt1-9BP2eH-ZY46WFEv02qSGFAdmw";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Communique avec l'API Gemini pour générer du contenu textuel.
     * 
     * @param prompt La requête à envoyer à l'IA.
     * @return La réponse générée par l'IA.
     */
    public static String askGemini(String prompt) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            JSONArray parts = new JSONArray();
            parts.put(part);
            JSONObject content = new JSONObject();
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                return jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text").trim();
            } else if (response.statusCode() == 429) {
                return "AI is currently busy (Quota exceeded). Please try again in a minute. ⏳";
            } else {
                System.err.println("Gemini API Error: " + response.body());
                return "AI logic is taking a break. Technical details: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Obtient un conseil de motivation court et percutant pour un objectif donné.
     * 
     * @param goalTitle   Titre de l'objectif.
     * @param description Description de l'objectif.
     * @return Un conseil personnalisé.
     */
    public static String getSmartGoalTip(String goalTitle, String description) {
        String prompt = "As a productivity coach, give me one short, punchy, and highly motivating tip for this goal: '"
                + goalTitle + "'. Background: " + description + ". Keep it under 25 words.";
        return askGemini(prompt);
    }

    /**
     * Fournit un conseil stratégique pour une méthode d'organisation choisie.
     * 
     * @param goalTitle Titre de l'objectif.
     * @param strategy  La stratégie utilisée (ex: "Focus").
     * @return Une explication sur l'efficacité de l'approche.
     */
    public static String getGoalStrategyAdvice(String goalTitle, String strategy) {
        String prompt = "I'm using a '" + strategy + "' strategy to achieve my goal: '" + goalTitle
                + "'. Explain in one sentence why this is a good approach for staying productivity.";
        return askGemini(prompt);
    }

    /**
     * Analyse les données de progression et prédit la probabilité de succès.
     * 
     * @param dataSummary Résumé textuel des données de progression.
     * @return Un pourcentage de réussite accompagné d'une recommandation.
     */
    public static String predictSuccessProbability(String dataSummary) {
        String prompt = "Analyze this goal progress data for a user: \n" + dataSummary + "\n" +
                "Provide a 'Success Probability' percentage and one short sentence recommending a capacity adjustment (e.g. increase to 4 daily actions) to reach 100%. "
                +
                "Format: 'XX% - [Recommendation]'. Keep it very short.";
        return askGemini(prompt);
    }
}
