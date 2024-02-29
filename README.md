# DreamScape-Server

<p>
    This is the server component for the DreamScape.ai application. It is written in Java 21 with Spring Boot. 
    This project wraps an <a href="https://github.com/reactiveclown/openai-webflux-java">OpenAI API client library</a> and exposes it as a REST API.
<br>    
    Developed as a term project in part of OSU CS 361: Software Engineering I while simulating Agile development methodologies.
</p>

<p>
    The server is designed to take a user's description of a dream they recently had and return an image that best matches that description using OpenAI's LLM and generative models such as GPT-3 and DALL-E.
</p>
<p>
    Users will interact with the server through the <a href="https://github.com/hc-lee/DreamScape-Client">DreamScape React GUI</a> which is live on Netlify at: 
<br> 
<a href="https://frabjous-heliotrope-3148d9.netlify.app/">https://frabjous-heliotrope-3148d9.netlify.app/</a>.
</p>


### Features

* Text prompt to image generation using GPT-3 and DALL-E models from OpenAI.
* Supports user prompts in 31 languages via <a href="https://en.wikipedia.org/wiki/DeepL_Translator">DeepL</a> translation. Credit to <a href="https://github.com/rob-cosentino">Robert Cosentino</a> for implementing the translation microservice.

### Planned Features

* Database integration to store user data as visual journal logs.
* User authentication and authorization.

## Usage

<p>

The server is currently deployed in a Docker container on an AWS EC2 instance. Currently, there is a single REST endpoint that accepts the user prompt and returns a URL to the generated image.

(POST) /generate_image

Example request body:
</p>

```courseignore
{
    "userPrompt" : "User description of image as a string"
}
```

<p>
    The server will then communicate with the OpenAI models using the API client library to return a URL to the image that best matches the user's description.
</p>

Example response body:
```courseignore
{
    "created" : (number),
    "data" : [
                { "url" : "https://oaidalleapiprodscus.blob.core.windows.net/private/..." }
             ]
}
```

## Details

<p>
When the generate_image endpoint is called, the language of the raw user input is detected and is conditionally translated to English (if not already) using the DeepL translation microservice. 
Since the OpenAI models are primarily trained on English, the translation step is necessary to ensure the best results.
</p>

<p>
Regardless if the prompt was translated, the prompt is then passed to GPT-3 to generate a short sentence(s) that is intended to capture the essence of the user's description into a prompt that is more
suitable to be passed to DALL-E. This step is taken because the DALL-E model takes declarative, descriptive, and short prompts. Passing the user's unadulterated description to DALL-E will result in less-accurate images.
</p>

<p>
Finally, the GPT-3 generated prompt is passed to DALL-E to generate the image. The image is then returned to the user as a URL.
</p>

The following diagram illustrates the flow of the server:

![](https://github.com/hc-lee/DreamScape-Server/blob/main/uml.png)

    2023 Hansol Lee
    
