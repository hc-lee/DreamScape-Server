package com.hansol.dreamscape;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@CrossOrigin
public class DreamScapeAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DreamScapeAiApplication.class, args);
	}

}
