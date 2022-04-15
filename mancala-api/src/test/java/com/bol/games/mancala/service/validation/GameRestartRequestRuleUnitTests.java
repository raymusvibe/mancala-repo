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
class GameRestartRequestRuleUnitTests {

    @Mock
    private MancalaRepository mancalaRepository;

    private static final GameRestartRequestRule gameRestartRequestRule = new GameRestartRequestRule();
    private static final GameRule dummyRule = new DummyRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoWinMove = new ClassPathResource("playerTwoWinMove.json");
    private final Resource playerTwoGameRestartMove = new ClassPathResource("playerTwoGameRestartMove.json");

    @BeforeAll
    public static void setUp () {
        gameRestartRequestRule.setSuccessor(dummyRule);
    }

    @Test
    void NewGameRequestRule_WhenNewGameRequest_NoValidationException () throws Exception {
        MancalaGame playerTwoNewGameMoveGame = mapper.readValue(resourceAsInputStream(playerTwoGameRestartMove), MancalaGame.class);
        MancalaGame playerTwoWinMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);

        assertDoesNotThrow(() -> gameRestartRequestRule
                .processRequest(playerTwoNewGameMoveGame, playerTwoWinMoveGame, mancalaRepository),
                "ValidationException not thrown");
    }
}