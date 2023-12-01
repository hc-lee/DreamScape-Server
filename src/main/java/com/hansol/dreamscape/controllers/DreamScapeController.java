package com.hansol.dreamscape.controllers;

import com.hansol.dreamscape.services.DreamScapeService;
import com.hansol.dreamscape.transferobjects.UserPromptDTO;
import io.github.reactiveclown.openaiwebfluxclient.client.images.CreateImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DreamScapeController {

    @Autowired
    DreamScapeService dreamScapeService;

    // Image generation endpoint. Takes a String and boolean as a request body and returns a Mono of CreateImageResponse.
    @CrossOrigin
    @PostMapping("/generate_image")
    public Mono<CreateImageResponse> generateImage(@RequestBody UserPromptDTO userPromptJSON) {
        try {
            return dreamScapeService.processPrompt(userPromptJSON.getUserPrompt());
        } catch (Exception e) {
            return Mono.error(e
                    .getCause()
                    .initCause(new Throwable("An error has occurred while generating your image.")));
        }

    }

}
