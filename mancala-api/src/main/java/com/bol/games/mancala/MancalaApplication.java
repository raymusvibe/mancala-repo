package com.bol.games.mancala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableMongoRepositories
@SpringBootApplication
@EnableWebMvc
public class MancalaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MancalaApplication.class, args);
	}

}
