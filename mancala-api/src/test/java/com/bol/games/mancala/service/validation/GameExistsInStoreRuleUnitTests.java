package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.exception.NotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GameExistsInStoreRuleUnitTests {
    @Mock
    private MancalaRepository mancalaRepository;

    private static final GameExistsInStoreRule gameExistsInStoreRule = new GameExistsInStoreRule();
    private static final GameRule dummyRule = new DummyRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoOppositeStoneCaptureMove = new ClassPathResource("playerTwoOppositeStoneCaptureMove.json");
    private final Resource playerTwoOppositeStoneCapturePriorMove = new ClassPathResource("playerTwoOppositeStoneCapturePriorMove.json");

    @BeforeAll
    public static void setUp () {
        gameExistsInStoreRule.setSuccessor(dummyRule);
    }

    @Test
    void GameExistsInStoreRule_WhenIncorrectGameId_NotFoundException () throws Exception {
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);

        assertThrows(NotFoundException.class, () -> gameExistsInStoreRule
                .processRequest(playerTwoOppositeStoneCaptureGame,
                        playerTwoOppositeStoneCapturePriorGame,
                        mancalaRepository),
                "NotFoundException was expected");
    }

    @Test
    void GameExistsInStoreRule_WhenValidGameId_NotFoundExceptionNotThrown () throws Exception {
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);

        doReturn(playerTwoOppositeStoneCapturePriorGame).when(mancalaRepository).findGame(any(String.class));

        assertDoesNotThrow(() -> gameExistsInStoreRule
                .processRequest(playerTwoOppositeStoneCaptureGame,
                        playerTwoOppositeStoneCapturePriorGame,
                        mancalaRepository),
                "NotFoundException not thrown");
    }
}
