package com.hansol.dreamscape.services;

import com.hansol.dreamscape.clients.OpenAiClients;
import com.hansol.dreamscape.exceptions.InvalidUserPromptException;
import com.hansol.dreamscape.exceptions.LanguageDetectLibraryException;
import io.github.reactiveclown.openaiwebfluxclient.client.images.CreateImageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(DreamScapeService.class)
class DreamScapeServiceTest {

    @MockBean
    private OpenAiClients openAiClients;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private WebTestClient webTestClient;


    @Autowired
    private DreamScapeService dreamScapeService;


    @Test
    void processPrompt_TakesString_ReturnsUrl() {

        // Given
        String userPrompt = "I dreamt I was a bird flying in the sky.";
        String expectedGpt3Prompt = "There is a bird that is flying in the sky";
        CreateImageResponse expectedDallEImageResponse = new CreateImageResponse(
                1703400324L,
                List.of(Map.of("url", "https://oaidalleapiprodscus.blob.core.windows.net/private/"))
        );

        // When
        when(openAiClients.generateGpt3Prompt(userPrompt))
                .thenReturn(expectedGpt3Prompt);
        when(openAiClients.generateDallEImage(expectedGpt3Prompt))
                .thenReturn(Mono.just(expectedDallEImageResponse));

        // Then
        Mono<CreateImageResponse> result = dreamScapeService.processPrompt(userPrompt);

        result.subscribe(createImageResponse -> {
            assertNotNull(createImageResponse.created());
            assertNotNull(createImageResponse.data());
            assertEquals(expectedDallEImageResponse.data(),
                    createImageResponse.data(),
                    "Expected CreateImageResponse.data to be equal to expectedDallEImageResponse.data");
            assertEquals(expectedDallEImageResponse.created(),
                    createImageResponse.created(),
                    "Expected CreateImageResponse.created to be equal to expectedDallEImageResponse.created");
        });


    }

    @Test
    void processPrompt_Throws_InvalidUserPromptException_OnInvalidPrompt() {
        // Given
        String userPrompt = "";

        // When and Then
        assertThrows(InvalidUserPromptException.class, () -> {
            dreamScapeService.processPrompt(userPrompt);
        });
    }

    @Test
    void detectLanguage_TakesString_ReturnsEnglishLanguageCode() {
        // Given
        String userPrompt = "I dreamt I was a bird flying in the sky.";

        // When
        String result = dreamScapeService.detectLanguage(userPrompt);

        // Then
        assertEquals("en", result);

    }

    @Test
    void detectLanguage_TakesString_DetectsNonEnglishPrompt_And_CallsTranslate() {
        // Given
        String userPrompt = "늘을 나는 새가 되는 꿈을 꾸었습니다.";

        // When
        String result = dreamScapeService.detectLanguage(userPrompt);

        // Then
        assertNotEquals("en", result);
    }

    @Test
    void detectLanguage_Throws_LanguageDetectLibraryException_OnError() {
        // Given (induce a library error with null input)
        String userPrompt = null;

        // When and Then
        assertThrows(LanguageDetectLibraryException.class, () -> {
            dreamScapeService.detectLanguage(userPrompt);
        });

    }

    // TO DO: Write tests for the translate() method. Make mock calls/responses for Rob's microservice.
}