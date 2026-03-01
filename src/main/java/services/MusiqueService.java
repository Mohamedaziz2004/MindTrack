package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class MusiqueService {

    // Vos identifiants FreeSound
    private static final String FREESOUND_API_URL = "https://freesound.org/apiv2/search/text/";
    private static final String FREESOUND_TOKEN = "ZcnxIrOg1mBYRq3mKntQYRrgQjouv3ZXCwrNAyLs"; // Votre API Key
    private static final String FREESOUND_CLIENT_ID = "QbsF1ufVO6xMCtAnmNpK"; // Votre Client ID

    private Random random;
    private List<String> playlistUrls;
    private int currentTrackIndex = 0;

    public MusiqueService() {
        this.random = new Random();
        this.playlistUrls = new ArrayList<>();
    }

    /**
     * Récupère une musique de relaxation depuis FreeSound
     */
    public String getMusiqueRelaxation() {
        try {
            String query = URLEncoder.encode("relaxation meditation calm", "UTF-8");
            String urlStr = FREESOUND_API_URL + "?query=" + query +
                    "&token=" + FREESOUND_TOKEN +
                    "&client_id=" + FREESOUND_CLIENT_ID +
                    "&fields=id,name,previews&page_size=10&duration=60";

            System.out.println("URL appelée: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            System.out.println("Code de réponse: " + responseCode);

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject json = new JSONObject(response.toString());
                System.out.println("Réponse JSON: " + json.toString());

                JSONArray results = json.getJSONArray("results");

                if (results.length() > 0) {
                    // Prendre un son aléatoire
                    int index = random.nextInt(results.length());
                    JSONObject sound = results.getJSONObject(index);

                    // Récupérer l'URL de prévisualisation
                    JSONObject previews = sound.getJSONObject("previews");
                    String previewUrl = previews.getString("preview-lq-mp3");
                    System.out.println("URL de prévisualisation: " + previewUrl);
                    return previewUrl;
                } else {
                    System.out.println("Aucun résultat trouvé");
                }
            } else {
                // Lire le message d'erreur
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                System.out.println("Erreur réponse: " + response.toString());
            }
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la musique: " + e.getMessage());
            e.printStackTrace();
        }

        // URLs de secours (musiques libres de droits)
        return getFallbackMusicUrl();
    }

    /**
     * Récupère une musique basée sur le type d'exercice
     */
    public String getMusiqueParType(String typeExercice) {
        String query = "";

        switch (typeExercice.toLowerCase()) {
            case "méditation":
            case "respiration":
                query = "meditation zen calm peaceful";
                break;
            case "yoga":
                query = "yoga relaxation soft";
                break;
            case "cardio":
                query = "energetic upbeat workout fast";
                break;
            case "renforcement":
                query = "motivational workout powerful";
                break;
            case "étirement":
                query = "stretching calm relaxing";
                break;
            default:
                query = "relaxation calm peaceful";
                break;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = FREESOUND_API_URL + "?query=" + encodedQuery +
                    "&token=" + FREESOUND_TOKEN +
                    "&client_id=" + FREESOUND_CLIENT_ID +
                    "&fields=id,name,previews&page_size=10&duration=60";

            System.out.println("Recherche pour: " + typeExercice + " - URL: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray results = json.getJSONArray("results");

                if (results.length() > 0) {
                    int index = random.nextInt(results.length());
                    JSONObject sound = results.getJSONObject(index);
                    JSONObject previews = sound.getJSONObject("previews");
                    return previews.getString("preview-lq-mp3");
                }
            }
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la musique: " + e.getMessage());
        }

        return getFallbackMusicUrl();
    }

    /**
     * Récupère une playlist de musiques pour un type d'exercice
     */
    public List<String> getPlaylistParType(String typeExercice, int nombreMorceaux) {
        List<String> playlist = new ArrayList<>();
        String query = "";

        switch (typeExercice.toLowerCase()) {
            case "méditation":
            case "respiration":
                query = "meditation zen calm peaceful";
                break;
            case "yoga":
                query = "yoga relaxation soft";
                break;
            case "cardio":
                query = "energetic upbeat workout fast";
                break;
            case "renforcement":
                query = "motivational workout powerful";
                break;
            case "étirement":
                query = "stretching calm relaxing";
                break;
            default:
                query = "relaxation calm peaceful";
                break;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = FREESOUND_API_URL + "?query=" + encodedQuery +
                    "&token=" + FREESOUND_TOKEN +
                    "&client_id=" + FREESOUND_CLIENT_ID +
                    "&fields=id,name,previews&page_size=" + nombreMorceaux +
                    "&duration=60";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray results = json.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject sound = results.getJSONObject(i);
                    JSONObject previews = sound.getJSONObject("previews");
                    playlist.add(previews.getString("preview-lq-mp3"));
                }
            }
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la playlist: " + e.getMessage());
        }

        // Si pas de résultats, ajouter des URLs de secours
        if (playlist.isEmpty()) {
            playlist.addAll(getFallbackPlaylist());
        }

        this.playlistUrls = playlist;
        return playlist;
    }

    /**
     * Récupère la musique suivante de la playlist
     */
    public String getMusiqueSuivante() {
        if (playlistUrls.isEmpty()) {
            return getFallbackMusicUrl();
        }

        currentTrackIndex = (currentTrackIndex + 1) % playlistUrls.size();
        return playlistUrls.get(currentTrackIndex);
    }

    /**
     * Récupère la musique précédente de la playlist
     */
    public String getMusiquePrecedente() {
        if (playlistUrls.isEmpty()) {
            return getFallbackMusicUrl();
        }

        currentTrackIndex = (currentTrackIndex - 1 + playlistUrls.size()) % playlistUrls.size();
        return playlistUrls.get(currentTrackIndex);
    }

    /**
     * URLs de secours (musiques libres de droits)
     */
    private String getFallbackMusicUrl() {
        String[] fallbackUrls = {
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
        };

        return fallbackUrls[random.nextInt(fallbackUrls.length)];
    }

    /**
     * Playlist de secours
     */
    private List<String> getFallbackPlaylist() {
        List<String> fallbackPlaylist = new ArrayList<>();
        fallbackPlaylist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
        fallbackPlaylist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3");
        fallbackPlaylist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3");
        fallbackPlaylist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3");
        fallbackPlaylist.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3");
        return fallbackPlaylist;
    }

    /**
     * Obtenir des recommandations musicales basées sur l'historique
     */
    public List<String> getRecommandationsMusicales(double scoreMoyen, double bienEtreMoyen) {
        List<String> recommandations = new ArrayList<>();

        if (bienEtreMoyen < 5.0) {
            recommandations.add("Musique apaisante pour réduire le stress");
            recommandations.add("Sons de la nature pour la relaxation");
        } else if (scoreMoyen < 60) {
            recommandations.add("Musique motivante pour améliorer la concentration");
            recommandations.add("Rythmes entraînants pour booster l'énergie");
        } else if (bienEtreMoyen >= 8.0) {
            recommandations.add("Musique joyeuse pour maintenir le bien-être");
            recommandations.add("Playlist de vos morceaux préférés");
        } else {
            recommandations.add("Musique d'ambiance pour la détente");
            recommandations.add("Sons binauraux pour la méditation");
        }

        return recommandations;
    }
}