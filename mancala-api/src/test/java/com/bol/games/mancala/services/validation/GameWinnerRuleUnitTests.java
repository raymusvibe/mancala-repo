package com.bol.games.mancala.services.validation;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.GameWinnerRule;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import com.bol.games.mancala.utils.DummyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class GameWinnerRuleUnitTests {

    @InjectMocks
    private MancalaRepository mancalaRepository;
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    private final GameWinnerRule gameWinnerRule = new GameWinnerRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoWinMove = new ClassPathResource("playerTwoWinMove.json");
    private final Resource playerTwoWinMissedMove = new ClassPathResource("playerTwoWinMissedMove.json");
    private final Resource playerTwoWinPriorMove = new ClassPathResource("playerTwoWinPriorMove.json");

    @BeforeEach
    public void setUp () {
        GameRule dummyRule = new DummyRule();
        gameWinnerRule.setSuccessor(dummyRule);
    }

    @Test
    void GameWinnerRule_WhenGenuineWinner_NoValidationException () throws Exception {
        MancalaGame playerTwoWinMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);
        MancalaGame playerTwoWinPriorMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);

        assertDoesNotThrow(() -> gameWinnerRule.
                processRequest(playerTwoWinMoveGame, playerTwoWinPriorMoveGame, mancalaRepository),
                "ValidationException not thrown");
    }

    @Test
    void GameWinnerRule_WhenFrontEndMissedWinner_DetermineWinner () throws Exception {
        MancalaGame playerTwoWinMissedMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMissedMove), MancalaGame.class);
        MancalaGame playerTwoWinPriorMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);

        assertDoesNotThrow(() -> gameWinnerRule.
                processRequest(playerTwoWinMissedMoveGame, playerTwoWinPriorMoveGame, mancalaRepository),
                "ValidationException not thrown");
    }
}
