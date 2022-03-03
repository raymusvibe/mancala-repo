package com.bol.games.mancala.services.validationrules;

import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validationrules.NewGameRequestRule;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class NewGameRequestRuleTests {
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;

    Rule newGameRequestRule = new NewGameRequestRule();

    private ObjectMapper mapper = new ObjectMapper();

    private final Resource playerTwoWinMove = new ClassPathResource("test/playerTwoWinMove.json");
    private final Resource playerTwoNewGameMove = new ClassPathResource("test/playerTwoNewGameMove.json");

    @BeforeEach
    public void setUp () {
        Rule dummyRule = new DummyRule();
        newGameRequestRule.setSuccessor(dummyRule);
    }

    @Test
    public void testValidationNewGame () throws Exception {
        MancalaGame playerTwoNewGameMoveGame = mapper.readValue(resourceAsInputStream(playerTwoNewGameMove), MancalaGame.class);
        MancalaGame playerTwoWinMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);

        assertDoesNotThrow(() -> {
            newGameRequestRule.processRequest(playerTwoNewGameMoveGame, playerTwoWinMoveGame, mancalaGamesMongoTemplate);
        }, "ValidationException not thrown");
    }
}