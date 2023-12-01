package com.hansol.dreamscape.services;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.ChatService;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.CreateChatCompletionRequest;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.CreateChatCompletionResponse;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.MessageData;
import io.github.reactiveclown.openaiwebfluxclient.client.images.CreateImageRequest;
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

import java.io.IOException;
import java.util.List;

@Service
public class DreamScapeService {

    // OpenAI images API client
    @Autowired
    io.github.reactiveclown.openaiwebfluxclient.client.images.ImagesService ImagesService;

    // OpenAI chat API client
    @Autowired
    ChatService chatService;

    public Mono<CreateImageResponse> processPrompt(String rawUserPrompt) throws IOException {

        String userPrompt = rawUserPrompt;

        //load all languages to detect:
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

        //build language detector:
        LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        //create a text object factory
        TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

        //query:
        TextObject textObject = textObjectFactory.forText(userPrompt);
        String lang = languageDetector.detect(textObject).get().getLanguage();

        try {

            if (!lang.equals("en")) {
                // Translate the non-English user prompt to English using Rob's microservice.
                System.out.println("Detected non-English prompt. Translating...");
                userPrompt = translate(userPrompt);
            }

            System.out.println("User prompt: " + userPrompt);

            // Process the user description into a suitable DALL·E prompt using GPT-3. Await response.
            CreateChatCompletionResponse response = chatService.createChatCompletion(
                    CreateChatCompletionRequest
                            .builder("gpt-3.5-turbo", List.of(new MessageData("user", "Describe this scene visually in less than 30 simple words. If personal pronouns are used, include the people: " + userPrompt)))
                            .n(1)
                            .build()
            ).block();

            // Some scuffed way of circumventing library bugs. (i.e. not being able to access choices.get(0).message())
            String dallEPrompt = response.choices().toString();
            int contentIndex = dallEPrompt.indexOf("content=");
            int endIndex = dallEPrompt.indexOf("], finishReason=stop]]");
            String content = dallEPrompt.substring(contentIndex + "content=".length(), endIndex);

            // Generate an image using the dall-e prompt.
            return ImagesService.createImage(
                    CreateImageRequest
                            .builder(content + " The image is realistic.")
                            .size("512x512")
                            .build());

        } catch (Error e) {
            // Error occurred while calling OpenAPI client.
            return Mono.error(e
                    .getCause()
                    .initCause(new Throwable("An error has occurred while generating your image.")));
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
            return "Error occurred while translating prompt.";
        }
    }

}