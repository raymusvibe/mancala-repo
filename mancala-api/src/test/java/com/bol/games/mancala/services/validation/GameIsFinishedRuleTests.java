package com.bol.games.mancala.services.validation;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.GameIsFinishedRule;
import com.bol.games.mancala.service.validation.abstractions.Rule;
import com.bol.games.mancala.utils.DummyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class GameIsFinishedRuleTests {
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;

    private Rule gameIsFinishedRule = new GameIsFinishedRule();
    private ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoWinMove = new ClassPathResource("test/playerTwoWinMove.json");
    private final Resource playerTwoWinPriorMove = new ClassPathResource("test/playerTwoWinPriorMove.json");
    private final Resource playerTwoWinInvalidStoneCountMove = new ClassPathResource("test/playerTwoWinInvalidStoneCountMove.json");

    @BeforeEach
    public void setUp () {
        Rule dummyRule = new DummyRule();
        gameIsFinishedRule.setSuccessor(dummyRule);
    }

    @Test
    public void testValidationWinner () throws Exception {
        MancalaGame playerTwoWinMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);
        MancalaGame playerTwoWinPriorMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);

        assertDoesNotThrow(() -> {
            gameIsFinishedRule.processRequest(playerTwoWinMoveGame, Optional.of(playerTwoWinPriorMoveGame), mancalaGamesMongoTemplate);
        }, "ValidationException not thrown");
    }

    @Test
    public void testValidationWinnerException () throws Exception {
        MancalaGame playerTwoWinInvalidStoneCountMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinInvalidStoneCountMove), MancalaGame.class);
        MancalaGame playerTwoWinPriorMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);

        assertThrows(ValidationException.class, () -> {
            gameIsFinishedRule.processRequest(playerTwoWinInvalidStoneCountMoveGame, Optional.of(playerTwoWinPriorMoveGame), mancalaGamesMongoTemplate);
        }, "ValidationException was expected");
    }
}
