package com.hansol.dreamscape.services;

import com.hansol.dreamscape.clients.OpenAiClients;
import com.hansol.dreamscape.exceptions.LanguageDetectLibraryException;
import com.hansol.dreamscape.exceptions.TranslationServiceException;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import io.github.reactiveclown.openaiwebfluxclient.client.images.CreateImageResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class DreamScapeService {

    @Autowired
    private OpenAiClients openAiClients;

    public Mono<CreateImageResponse> processPrompt(String rawUserPrompt) {

        Date currentDate = new Date();
        String formattedDate = currentDate.toString();
        System.out.println("=========================================");
        System.out.println(formattedDate);

        if (Objects.equals(rawUserPrompt, "") || rawUserPrompt == null) {
            System.out.println("Empty user prompt detected");
            throw new NullPointerException("User prompt is empty or null.");
        }

        String userPrompt = rawUserPrompt;

        try {
            // Detect the language of the user prompt and translate it to English if necessary.
            String lang = detectLanguage(userPrompt);

            if (!lang.equals("en")) {
                // Translate the non-English user prompt to English using Rob's microservice.
                System.out.println("Detected non-English prompt. Translating...");
                userPrompt = translate(userPrompt);
            }
            System.out.println("User prompt: " + userPrompt);

            // Call the GPT3 model to generate a dall-e prompt.
            String content = openAiClients.generateGpt3Prompt(userPrompt);

            // Generate an image using the dall-e prompt.
            return openAiClients.generateDallEImage(content);

        } catch (Exception e) {
            System.out.println("Error occurred in the image generation service.");
            return Mono.error(e);
        }
    }

    // Make a translation request to Rob's microservice.
    public String translate(String nonEnglishPrompt) {
        System.out.println("Translating prompt: " + nonEnglishPrompt);

        // Build an HTTP POST request to Rob's microservice.
        String apiUrl = "https://deepl-translator-jwmr.onrender.com/translate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"payload\": \"" + nonEnglishPrompt + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            JSONObject responseJSON = new JSONObject(responseEntity.getBody());
            return responseJSON.getString("translated");

        } catch (Exception e) {
            System.out.println("Error occurred while translating prompt.");
            throw new TranslationServiceException("Error occurred while translating prompt.");
        }
    }

    // Helper function to detect the language of a string.
    public String detectLanguage(String prompt) {
        try {
            // Load all languages to detect
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

            // Build language detector
            LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(languageProfiles)
                    .build();
            TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

            // Query
            TextObject textObject = textObjectFactory.forText(prompt);

            return languageDetector.detect(textObject).get().getLanguage();

        } catch (Exception e) {
            System.out.println("Error occurred while detecting language.");
            throw new LanguageDetectLibraryException("Error occurred while detecting language.");
        }
    }

}