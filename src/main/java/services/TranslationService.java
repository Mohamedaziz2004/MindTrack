package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * TranslationService - Detects language and translates text to English using MyMemory API (free, no auth needed)
 */
public class TranslationService {
    // Using MyMemory API - free translation service, no authentication required
    private static final String MYMEMORY_TRANSLATE_URL = "https://api.mymemory.translated.net/get";
    private static final String MYMEMORY_DETECT_URL = "https://api.mymemory.translated.net/detect";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final int API_CALL_DELAY = 500; // Minimum delay between API calls (ms)
    private static long lastApiCallTime = 0;

    /**
     * Enforce rate limiting between API calls
     */
    private synchronized void enforceRateLimit() {
        long timeSinceLastCall = System.currentTimeMillis() - lastApiCallTime;
        if (timeSinceLastCall < API_CALL_DELAY) {
            try {
                Thread.sleep(API_CALL_DELAY - timeSinceLastCall);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastApiCallTime = System.currentTimeMillis();
    }

    /**
     * Detect the language of the given text
     * @param text The text to detect language for
     * @return The detected language code (e.g., "en", "ar", "fr")
     */
    public String detectLanguage(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }

        // Use character-based detection - fast, reliable, no API call needed
        String detectedLang = detectLanguageSimple(text);
        System.out.println("Detected language from character analysis: " + detectedLang);
        return detectedLang;
    }

    /**
     * Simple language detection based on character patterns
     * @param text The text to detect language for
     * @return The detected language code
     */
    private String detectLanguageSimple(String text) {
        if (text == null || text.isEmpty()) return "en";

        int arabicCount = 0;
        int frenchCount = 0;
        int spanishCount = 0;
        int chineseCount = 0;
        int japaneseCount = 0;

        for (char c : text.toCharArray()) {
            // Arabic Unicode range: U+0600 to U+06FF
            if (c >= 0x0600 && c <= 0x06FF) {
                arabicCount++;
            }
            // French accents and characters
            if (c == 'é' || c == 'è' || c == 'ê' || c == 'ë' || c == 'à' || c == 'â' || c == 'ä' ||
                c == 'ù' || c == 'û' || c == 'ü' || c == 'ô' || c == 'ö' || c == 'ç') {
                frenchCount++;
            }
            // Spanish accents
            if (c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'ü' || c == 'ñ' || c == '¿' || c == '¡') {
                spanishCount++;
            }
            // CJK characters
            if ((c >= 0x4E00 && c <= 0x9FFF) || (c >= 0x3400 && c <= 0x4DBF)) {
                chineseCount++;
            }
            if ((c >= 0x3040 && c <= 0x309F) || (c >= 0x30A0 && c <= 0x30FF)) {
                japaneseCount++;
            }
        }

        if (arabicCount > text.length() * 0.1) return "ar";
        if (frenchCount > text.length() * 0.05) return "fr";
        if (spanishCount > text.length() * 0.05) return "es";
        if (chineseCount > text.length() * 0.1) return "zh";
        if (japaneseCount > text.length() * 0.1) return "ja";

        return "en"; // Default to English
    }

    /**
     * Translate text to English from any language
     * @param text The text to translate
     * @return The translated English text
     */
    public String translateToEnglish(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Detect the source language
        String sourceLanguage = detectLanguage(text);

        // If already in English, return as is
        if ("en".equalsIgnoreCase(sourceLanguage)) {
            System.out.println("Text is already in English, no translation needed");
            return text;
        }

        // Translate to English
        return translate(text, sourceLanguage, "en");
    }

    /**
     * Translate text from source language to target language using MyMemory API (free, no auth required)
     * @param text The text to translate
     * @param sourceLang The source language code (e.g., "ar", "fr")
     * @param targetLang The target language code (e.g., "en")
     * @return The translated text
     */
    public String translate(String text, String sourceLang, String targetLang) throws Exception {
        // If source and target are the same, no need to translate
        if (sourceLang.equalsIgnoreCase(targetLang)) {
            System.out.println("Source and target languages are the same, returning original text");
            return text;
        }

        try {
            enforceRateLimit(); // Prevent rate limit errors

            // MyMemory API endpoint format: /get?q=text&langpair=source|target
            String encodedText = java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
            String encodedLangPair = java.net.URLEncoder.encode(sourceLang + "|" + targetLang, StandardCharsets.UTF_8);

            // Build URI using proper constructor to avoid illegal character issues
            URI uri;
            try {
                String query = "q=" + encodedText + "&langpair=" + encodedLangPair;
                uri = new URI(MYMEMORY_TRANSLATE_URL + "?" + query);
            } catch (URISyntaxException e) {
                System.err.println("❌ Failed to build URI: " + e.getMessage());
                return attemptFallbackTranslation(text, sourceLang);
            }

            System.out.println("\n=== Translation Request (MyMemory API) ===");
            System.out.println("Source: " + sourceLang + " → Target: " + targetLang);
            System.out.println("Text: " + text);
            System.out.println("URI: " + uri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("User-Agent", "MindTrack-App/1.0")
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                try {
                    JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

                    // Check if translation was successful
                    int responseStatus = jsonResponse.get("responseStatus").getAsInt();
                    if (responseStatus == 200) {
                        JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
                        String translatedText = responseData.get("translatedText").getAsString();
                        System.out.println("✓ Translation successful: " + translatedText);
                        System.out.println("=== End Translation ===\n");
                        return translatedText;
                    } else {
                        System.err.println("Translation API error: Status " + responseStatus);
                        return attemptFallbackTranslation(text, sourceLang);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing response: " + e.getMessage());
                    e.printStackTrace();
                    return attemptFallbackTranslation(text, sourceLang);
                }
            } else if (response.statusCode() == 429) {
                System.err.println("⚠ Rate limit (429) - Too many requests");
                Thread.sleep(2000);
                return attemptFallbackTranslation(text, sourceLang);
            } else {
                System.err.println("❌ Translation failed: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return attemptFallbackTranslation(text, sourceLang);
            }
        } catch (Exception e) {
            System.err.println("❌ Translation exception: " + e.getMessage());
            e.printStackTrace();
            return attemptFallbackTranslation(text, sourceLang);
        }
    }

    /**
     * Fallback translation using simple word mapping for common phrases
     * @param text The text to translate
     * @param sourceLang The source language
     * @return Fallback translation or original text
     */
    private String attemptFallbackTranslation(String text, String sourceLang) {
        System.out.println("\n⚠ API unavailable or limit reached, attempting fallback translation...");

        // Simple fallback dictionary for common Arabic phrases and words
        if ("ar".equalsIgnoreCase(sourceLang)) {
            java.util.Map<String, String> arabicToEnglish = new java.util.HashMap<>();

            // Common phrases and words
            arabicToEnglish.put("السعادة", "happiness");
            arabicToEnglish.put("الأشياء", "things");
            arabicToEnglish.put("البسيطة", "simple");
            arabicToEnglish.put("في", "in");
            arabicToEnglish.put("من", "from");
            arabicToEnglish.put("جدّ", "strove");
            arabicToEnglish.put("جد", "strove");
            arabicToEnglish.put("وجد", "found");
            arabicToEnglish.put("مرحبا", "hello");
            arabicToEnglish.put("شكرا", "thank you");
            arabicToEnglish.put("من فضلك", "please");
            arabicToEnglish.put("نعم", "yes");
            arabicToEnglish.put("لا", "no");
            arabicToEnglish.put("أنا", "I");
            arabicToEnglish.put("أنت", "you");
            arabicToEnglish.put("هو", "he");
            arabicToEnglish.put("هي", "she");
            arabicToEnglish.put("نحن", "we");
            arabicToEnglish.put("هم", "they");
            arabicToEnglish.put("واو", "and");
            arabicToEnglish.put("و", "and");
            arabicToEnglish.put("أو", "or");
            arabicToEnglish.put("ب", "with");
            arabicToEnglish.put("على", "on");
            arabicToEnglish.put("إلى", "to");
            arabicToEnglish.put("عن", "about");
            arabicToEnglish.put("حول", "about");
            arabicToEnglish.put("الذي", "which");
            arabicToEnglish.put("الذين", "who");
            arabicToEnglish.put("هذا", "this");
            arabicToEnglish.put("ذلك", "that");
            arabicToEnglish.put("هنا", "here");
            arabicToEnglish.put("هناك", "there");
            arabicToEnglish.put("الآن", "now");
            arabicToEnglish.put("أمس", "yesterday");
            arabicToEnglish.put("غدا", "tomorrow");
            arabicToEnglish.put("اليوم", "today");
            arabicToEnglish.put("الليل", "night");
            arabicToEnglish.put("النهار", "day");
            arabicToEnglish.put("الصباح", "morning");
            arabicToEnglish.put("المساء", "evening");
            arabicToEnglish.put("الرجل", "man");
            arabicToEnglish.put("المرأة", "woman");
            arabicToEnglish.put("الولد", "boy");
            arabicToEnglish.put("البنت", "girl");
            arabicToEnglish.put("الكتاب", "book");
            arabicToEnglish.put("الجدول", "table");
            arabicToEnglish.put("الباب", "door");
            arabicToEnglish.put("النافذة", "window");
            arabicToEnglish.put("الماء", "water");
            arabicToEnglish.put("النار", "fire");
            arabicToEnglish.put("الهواء", "air");
            arabicToEnglish.put("العمل", "work");
            arabicToEnglish.put("الوقت", "time");
            arabicToEnglish.put("الحياة", "life");
            arabicToEnglish.put("الحب", "love");
            arabicToEnglish.put("الصداقة", "friendship");
            arabicToEnglish.put("الخوف", "fear");
            arabicToEnglish.put("الأمل", "hope");
            arabicToEnglish.put("الحزن", "sadness");
            arabicToEnglish.put("الفرح", "joy");
            arabicToEnglish.put("القوة", "strength");
            arabicToEnglish.put("الضعف", "weakness");
            arabicToEnglish.put("الذكاء", "intelligence");
            arabicToEnglish.put("الجهل", "ignorance");
            arabicToEnglish.put("الحق", "truth");
            arabicToEnglish.put("الكذب", "lie");
            arabicToEnglish.put("العدل", "justice");
            arabicToEnglish.put("الظلم", "injustice");
            arabicToEnglish.put("الخير", "good");
            arabicToEnglish.put("الشر", "evil");
            arabicToEnglish.put("الجمال", "beauty");
            arabicToEnglish.put("القبح", "ugliness");
            arabicToEnglish.put("الصحة", "health");
            arabicToEnglish.put("المرض", "illness");
            arabicToEnglish.put("الألم", "pain");
            arabicToEnglish.put("الراحة", "comfort");
            arabicToEnglish.put("العلم", "knowledge");
            arabicToEnglish.put("المدرسة", "school");
            arabicToEnglish.put("الجامعة", "university");
            arabicToEnglish.put("المنزل", "home");
            arabicToEnglish.put("الشارع", "street");
            arabicToEnglish.put("الحقل", "field");
            arabicToEnglish.put("البحر", "sea");
            arabicToEnglish.put("الجبل", "mountain");
            arabicToEnglish.put("النهر", "river");
            arabicToEnglish.put("الشمس", "sun");
            arabicToEnglish.put("القمر", "moon");
            arabicToEnglish.put("النجم", "star");
            arabicToEnglish.put("السماء", "sky");
            arabicToEnglish.put("الأرض", "earth");

            // Use array to allow mutation in lambda (workaround for final requirement)
            final String[] result = { text };

            // Sort entries by length (longest first) to handle compound words better
            arabicToEnglish.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()))
                    .forEach(entry -> {
                        result[0] = result[0].replaceAll(java.util.regex.Pattern.quote(entry.getKey()), entry.getValue());
                    });

            if (!result[0].equals(text)) {
                System.out.println("✓ Fallback translation: " + result[0]);
                return result[0];
            }
        }

        System.out.println("✗ No translation available in dictionary, returning original text");
        return text;
    }


    /**
     * Translate text with automatic language detection and return both original and translated
     * @param text The text to process
     * @return An object containing language info and translated text
     */
    public TranslationResult processText(String text) throws Exception {
        String detectedLang = detectLanguage(text);
        String translated = text;
        boolean wasTranslated = false;

        // If not in English, translate to English
        if (!"en".equalsIgnoreCase(detectedLang)) {
            System.out.println("\n--- Starting Translation ---");
            System.out.println("Detected language: " + detectedLang);
            System.out.println("Original text: " + text);

            translated = translate(text, detectedLang, "en");
            wasTranslated = !translated.equals(text); // Check if translation actually changed the text

            if (wasTranslated) {
                System.out.println("Translation completed successfully!");
                System.out.println("Translated text: " + translated);
            } else {
                System.out.println("Translation failed or returned original text");
            }
            System.out.println("--- End Translation ---\n");
        } else {
            System.out.println("Text is already in English, no translation needed");
        }

        return new TranslationResult(text, translated, detectedLang, wasTranslated);
    }

    /**
     * Inner class to hold translation results
     */
    public static class TranslationResult {
        public final String originalText;
        public final String translatedText;
        public final String detectedLanguage;
        public final boolean wasTranslated;

        public TranslationResult(String originalText, String translatedText, String detectedLanguage, boolean wasTranslated) {
            this.originalText = originalText;
            this.translatedText = translatedText;
            this.detectedLanguage = detectedLanguage;
            this.wasTranslated = wasTranslated;
        }

        @Override
        public String toString() {
            return "TranslationResult{" +
                    "originalText='" + originalText + '\'' +
                    ", translatedText='" + translatedText + '\'' +
                    ", detectedLanguage='" + detectedLanguage + '\'' +
                    ", wasTranslated=" + wasTranslated +
                    '}';
        }
    }
}

