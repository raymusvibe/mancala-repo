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

@ExtendWith(MockitoExtension.class)
class GameRestartRequestRuleUnitTests {

    @Mock
    private MancalaRepository mancalaRepository;

    private static final GameRestartRequestRule gameRestartRequestRule = new GameRestartRequestRule();
    private static final GameRule dummyRule = new DummyRule();

    @BeforeAll
    public static void setUp () {
        gameRestartRequestRule.setSuccessor(dummyRule);
    }

    @Test
    void NewGameRequestRule_WhenNewGameRequest_NoStatusRelatedValidationException () {
        MancalaGame game = new MancalaGame();
        game.setGamePlayStatus(GameStatus.FINISHED);
        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.RESTARTING, null);

        assertDoesNotThrow(() -> gameRestartRequestRule
                .executeRule(gamePlay, game, mancalaRepository),
                "ValidationException not thrown");
    }

    @Test
    void NewGameRequestRule_WhenNewGameRequest_StatusRelatedValidationExceptionExpected () {
        MancalaGame game = new MancalaGame();
        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.RESTARTING, null);

        assertThrows(ValidationException.class,() -> gameRestartRequestRule
                        .executeRule(gamePlay, game, mancalaRepository),
                "ValidationException was expected");
    }
}