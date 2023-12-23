package com.hansol.dreamscape.clients;

import com.hansol.dreamscape.exceptions.DallEImageGenerateException;
import com.hansol.dreamscape.exceptions.GptPromptGenerateException;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.ChatService;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.CreateChatCompletionRequest;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.CreateChatCompletionResponse;
import io.github.reactiveclown.openaiwebfluxclient.client.chat.MessageData;
import io.github.reactiveclown.openaiwebfluxclient.client.images.CreateImageRequest;
import io.github.reactiveclown.openaiwebfluxclient.client.images.CreateImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OpenAiClients {
    // OpenAI images API client
    @Autowired
    private io.github.reactiveclown.openaiwebfluxclient.client.images.ImagesService ImagesService;

    // OpenAI chat API client
    @Autowired
    private ChatService chatService;


    public String generateGpt3Prompt(String userPrompt) {

        try {
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

            return content;

        } catch (Exception e) {
            System.out.println("Error generating GPT prompt: " + e.getMessage());
            throw new GptPromptGenerateException("Error generating GPT prompt: " + e.getMessage());
        }

    }


    public Mono<CreateImageResponse> generateDallEImage(String content) {

        try {
            // Generate an image using the dall-e prompt.
            return ImagesService.createImage(
                    CreateImageRequest
                            .builder(content + " The image is realistic.")
                            .size("512x512")
                            .build());

        } catch (Exception e) {
            System.out.println("Error generating Dall-E image: " + e.getMessage());
            throw new DallEImageGenerateException("Error generating Dall-E image: " + e.getMessage());
        }

    }

}
