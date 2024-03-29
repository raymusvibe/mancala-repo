package com.bol.games.mancala.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port}")
    private String mongoPort;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(String.format("mongodb://%s:%d", mongoHost, Integer.parseInt(mongoPort)));
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
