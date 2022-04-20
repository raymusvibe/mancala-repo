package com.bol.games.mancala.repository;

import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class MancalaRepositoryIntegrationTests {
    @Autowired
    private MancalaTestRepository mancalaTestRepository;

    @Container
    public static MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:4.4.3"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", container::getHost);
        registry.add("spring.data.mongodb.port", container::getFirstMappedPort);
    }

    @BeforeAll
    static void initAll(){
        container.start();
    }

    @AfterEach
    void cleanUp() {
        mancalaTestRepository.deleteAll();
    }

    @Test
    void MancalaRepository_WhenContainerStarts_PublicPortIsAvailable() {
        assertThatPortIsAvailable(container);
    }

    @Test
    void MancalaRepository_WhenApplicationWritesAndReadFromRepository_DataInCorrectState() {
        MancalaGame game = new MancalaGame();
        game.initialiseBoardToStartNewGame();
        MancalaGame savedGame = mancalaTestRepository.save(game);
        assertThat(mancalaTestRepository.findAll()).hasSize(1);
        assertThat(savedGame.getId()).isNotNull();
        assertThat(savedGame.getGameId()).isNotNull();
        assertThat(savedGame.getActivePlayer()).isEqualByComparingTo(Player.PLAYER_ONE);
    }

    private void assertThatPortIsAvailable(MongoDBContainer container) {
        try {
            new Socket(container.getHost(), container.getFirstMappedPort());
        } catch (IOException e) {
            throw new AssertionError("The expected port " + container.getFirstMappedPort() + " is not available!");
        }
    }
}
