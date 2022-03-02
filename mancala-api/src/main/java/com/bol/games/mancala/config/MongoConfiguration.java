package com.bol.games.mancala.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfiguration {
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }

    @Bean
    public MongoTemplate mancalaGamesMongoTemplate() {
        return new MongoTemplate(mongoClient(), "mancala-games-test");
    }

    @Bean
    public MongoTemplate mancalaEventsMongoTemplate() {
        return new MongoTemplate(mongoClient(), "mancala-games-events-test");
    }
}
