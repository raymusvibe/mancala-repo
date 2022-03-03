package com.bol.games.mancala.services.validationrules;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validationrules.StoneCountRule;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
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

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class StoneCountRuleTests {
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;

    private Rule stoneCountRule = new StoneCountRule();
    private ObjectMapper mapper = new ObjectMapper();
    private final Resource playerOneFirstMoveInvalidStoneCountMove = new ClassPathResource("test/playerOneFirstMoveInvalidStoneCountMove.json");
    private final Resource playerTwoWinMove = new ClassPathResource("test/playerTwoWinMove.json");

    @BeforeEach
    public void setUp () {
        Rule dummyRule = new DummyRule();
        stoneCountRule.setSuccessor(dummyRule);
    }

    @Test
    public void testValidationStoneCount () throws Exception {
        MancalaGame playerOneFirstMoveInvalidStoneCountMoveGame = mapper.readValue(resourceAsInputStream(playerOneFirstMoveInvalidStoneCountMove), MancalaGame.class);
        MancalaGame playerTwoWinMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);

        assertThrows(ValidationException.class, () -> {
            stoneCountRule.processRequest(playerOneFirstMoveInvalidStoneCountMoveGame, playerTwoWinMoveGame, mancalaGamesMongoTemplate);
        }, "ValidationException was expected");
    }
}
