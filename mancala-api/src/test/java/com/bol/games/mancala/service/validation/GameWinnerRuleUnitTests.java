package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import com.bol.games.mancala.util.DummyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static com.bol.games.mancala.util.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class GameWinnerRuleUnitTests {

    @Mock
    private MancalaRepository mancalaRepository;

    private static final GameWinnerRule gameWinnerRule = new GameWinnerRule();
    private static final GameRule dummyRule = new DummyRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoWinMove = new ClassPathResource("playerTwoWinMove.json");
    private final Resource playerTwoWinMissedMove = new ClassPathResource("playerTwoWinMissedMove.json");
    private final Resource playerTwoWinPriorMove = new ClassPathResource("playerTwoWinPriorMove.json");

    @BeforeAll
    public static void setUp () {
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
