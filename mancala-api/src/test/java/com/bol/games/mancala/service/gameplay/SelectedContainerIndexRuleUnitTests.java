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

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SelectedContainerIndexRuleUnitTests {

    @Mock
    private MancalaRepository mancalaRepository;

    private static final SelectedContainerIndexRule selectedContainerIndexRule = new SelectedContainerIndexRule();
    private static final GameRule dummyRule = new DummyRule();

    @BeforeAll
    public static void setUp () {
        selectedContainerIndexRule.setSuccessor(dummyRule);
    }

    @Test
    void SelectedContainerIndexRule_WhenOpponentsContainerSelected_ValidationException() {
        MancalaGame game = new MancalaGame();
        game.initialiseBoardToStartNewGame();
        game.setGamePlayStatus(GameStatus.IN_PROGRESS);
        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.IN_PROGRESS, 12);

        assertThrows(ValidationException.class,() -> selectedContainerIndexRule
                        .executeRule(gamePlay, game, mancalaRepository),
                        "ValidationException was expected");
    }
}