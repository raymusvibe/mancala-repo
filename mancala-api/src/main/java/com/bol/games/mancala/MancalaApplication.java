package com.bol.games.mancala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@SpringBootApplication (exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableCaching
public class MancalaApplication {
	public static void main(String[] args) {
		SpringApplication.run(MancalaApplication.class, args);
	}
}
