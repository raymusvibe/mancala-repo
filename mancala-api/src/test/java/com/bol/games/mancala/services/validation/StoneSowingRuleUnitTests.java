package com.bol.games.mancala.services.validation;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.StoneSowingRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StoneSowingRuleUnitTests {
    @Mock
    private MancalaRepository mancalaRepository;

    private final StoneSowingRule stoneSowingRule = new StoneSowingRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoOppositeStoneCaptureMove = new ClassPathResource("playerTwoOppositeStoneCaptureMove.json");
    private final Resource playerTwoOppositeStoneCapturePriorMove = new ClassPathResource("playerTwoOppositeStoneCapturePriorMove.json");
    private final Resource playerOneFirstMove = new ClassPathResource("playerOneFirstMove.json");

    @Test
    void StoneSowingRule_WhenStoneSowingError_ValidationException () throws Exception {
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);
        MancalaGame playerOneFirstMoveGame = mapper.readValue(resourceAsInputStream(playerOneFirstMove), MancalaGame.class);


        assertThrows(ValidationException.class, () -> stoneSowingRule
                .processRequest(playerTwoOppositeStoneCaptureGame,
                        playerOneFirstMoveGame,
                        mancalaRepository),
                "ValidationException was expected");
    }

    @Test
    void StoneSowingRule_WhenOppositeStoneCapturedCorrectly_NoValidationException () throws Exception {
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);

        assertDoesNotThrow(() -> stoneSowingRule
                .processRequest(playerTwoOppositeStoneCaptureGame,
                        playerTwoOppositeStoneCapturePriorGame,
                        mancalaRepository),
                "ValidationException not thrown");
    }
}