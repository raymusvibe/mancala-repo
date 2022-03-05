package com.bol.games.mancala.services.validation;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.StoneSowingRule;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
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
class StoneSowingRuleTests {
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;

    private final GameRule stoneSowingRule = new StoneSowingRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoOppositeStoneCaptureMove = new ClassPathResource("test/playerTwoOppositeStoneCaptureMove.json");
    private final Resource playerTwoOppositeStoneCapturePriorMove = new ClassPathResource("test/playerTwoOppositeStoneCapturePriorMove.json");
    private final Resource playerOneFirstMove = new ClassPathResource("test/playerOneFirstMove.json");

    @BeforeEach
    public void setUp () {
        GameRule dummyRule = new DummyRule();
        stoneSowingRule.setSuccessor(dummyRule);
    }

    @Test
    void testValidationSimulationFailure () throws Exception {
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);
        MancalaGame playerOneFirstMoveGame = mapper.readValue(resourceAsInputStream(playerOneFirstMove), MancalaGame.class);


        assertThrows(ValidationException.class, () -> stoneSowingRule
                .processRequest(playerTwoOppositeStoneCaptureGame,
                        Optional.of(playerOneFirstMoveGame),
                        mancalaGamesMongoTemplate),
                "ValidationException was expected");
    }

    @Test
    void testValidationOppositeStone () throws Exception {
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);

        assertDoesNotThrow(() -> stoneSowingRule
                .processRequest(playerTwoOppositeStoneCaptureGame, Optional.of(playerTwoOppositeStoneCapturePriorGame), mancalaGamesMongoTemplate),
                "ValidationException not thrown");
    }
}