package com.hansol.dreamscape.controllers;

import com.hansol.dreamscape.services.DreamScapeService;
import com.hansol.dreamscape.transferobjects.UserPromptDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DreamScapeController {

    @Autowired
    private DreamScapeService dreamScapeService;

    // Image generation endpoint. Takes a String and boolean as a request body and returns a Mono of CreateImageResponse.
    @CrossOrigin
    @PostMapping("/generate_image")
    public Mono<?> generateImage(@RequestBody UserPromptDTO userPromptJSON) {
        return dreamScapeService.processPrompt(userPromptJSON.getUserPrompt());
    }

}
