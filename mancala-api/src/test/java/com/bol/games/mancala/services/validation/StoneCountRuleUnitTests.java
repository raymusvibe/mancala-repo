package com.bol.games.mancala.services.validation;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.StoneCountRule;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import com.bol.games.mancala.utils.DummyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StoneCountRuleUnitTests {
    @Mock
    private MancalaRepository mancalaRepository;
    private static final StoneCountRule stoneCountRule = new StoneCountRule();
    private static final GameRule dummyRule = new DummyRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerOneFirstMoveInvalidStoneCountMove = new ClassPathResource("playerOneFirstMoveInvalidStoneCountMove.json");
    private final Resource playerTwoWinMove = new ClassPathResource("playerTwoWinMove.json");

    @BeforeAll
    public static void setUp () {
        stoneCountRule.setSuccessor(dummyRule);
    }

    @Test
    void StoneCountRule_WhenInValidStoneCount_ValidationException () throws Exception {
        MancalaGame playerOneFirstMoveInvalidStoneCountMoveGame = mapper.readValue(resourceAsInputStream(playerOneFirstMoveInvalidStoneCountMove), MancalaGame.class);
        MancalaGame playerTwoWinMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);

        assertThrows(ValidationException.class, () -> stoneCountRule
                .processRequest(playerOneFirstMoveInvalidStoneCountMoveGame,
                        playerTwoWinMoveGame,
                        mancalaRepository),
                "ValidationException was expected");
    }
}
