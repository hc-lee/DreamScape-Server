package com.hansol.dreamscape.controllers;

import com.hansol.dreamscape.services.DreamScapeService;
import io.github.reactiveclown.openaiwebfluxclient.client.images.CreateImageResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

// JUNIT 5!! not 4.
@ExtendWith(MockitoExtension.class)
@WebFluxTest(DreamScapeController.class)
public class DreamScapeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DreamScapeService dreamScapeService;

    @Test
    void test_DreamScapeController_GenerateImage_Returns_ImageUrl() throws Exception{

        // Mock a response from the OpenAI client.
        CreateImageResponse mockResponse = new CreateImageResponse(
                1703400324L,
                List.of(Map.of("url", "https://oaidalleapiprodscus.blob.core.windows.net/private/"))
        );

        // When
        when(dreamScapeService.processPrompt("I dreamt I was a bird flying in the sky."))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.post()
                .uri("/generate_image")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userPrompt\": \"I dreamt I was a bird flying in the sky.\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.created").isEqualTo(1703400324L)
                .jsonPath("$.data[0].url").value(Matchers.containsString("https://oaidalleapiprodscus.blob.core.windows.net/private/"));
    }

    @Test
    void test_DreamScapeController_GenerateImage_Successfully_Throws_Exception() throws Exception {

        // When
        when(dreamScapeService.processPrompt(""))
                .thenThrow(new NullPointerException("User prompt is empty or null."));

        // Then
        webTestClient.post()
                .uri("/generate_image")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userPrompt\": \"\"}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("500: INTERNAL_SERVER_ERROR")
                .jsonPath("$.errorMessage").isEqualTo("User prompt is empty or null.");
    }


}