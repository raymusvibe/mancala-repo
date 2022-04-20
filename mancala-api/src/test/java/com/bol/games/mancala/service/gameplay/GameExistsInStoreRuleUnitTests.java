package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;
import com.bol.games.mancala.util.DummyRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private static final String someInvalidGameId = "some_invalid_game_id";

    @BeforeAll
    public static void setUp () {
        gameExistsInStoreRule.setSuccessor(dummyRule);
    }

    @Test
    void GameExistsInStoreRule_WhenIncorrectGameId_ValidationException () {
        GamePlay gamePlay = new GamePlay(someInvalidGameId, GameStatus.IN_PROGRESS, 11);

        assertThrows(ValidationException.class, () -> gameExistsInStoreRule
                .executeRule(gamePlay, null, mancalaRepository),
                "ValidationException was expected");
    }

    @Test
    void GameExistsInStoreRule_WhenValidGameId_ExceptionNotThrown () {
        MancalaGame expectedGame = new MancalaGame();
        doReturn(expectedGame).when(mancalaRepository).findGame(any(String.class));

        GamePlay gamePlay = new GamePlay(expectedGame.getGameId(), GameStatus.IN_PROGRESS, 11);

        assertDoesNotThrow(() -> gameExistsInStoreRule
                .executeRule(gamePlay, null, mancalaRepository),
                "ValidationException not thrown");
    }
}
