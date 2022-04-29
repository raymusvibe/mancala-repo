package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.GameWinner;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static com.bol.games.mancala.util.ResourceReader.resourceAsInputStream;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GameWinnerRuleUnitTests {

    @Mock
    private MancalaRepository mancalaRepository;

    private static final GameWinnerRule gameWinnerRule = new GameWinnerRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource stateBeforePlayerTwoWin = new ClassPathResource("gameStateBeforePlayerTwoWin.json");
    private final Resource stateBeforeDraw = new ClassPathResource("gameStateBeforeDraw.json");

    @Test
    void GameWinnerRule_WhenGenuineWinner_GameStatusChangesToFinished () throws Exception {
        MancalaGame game = mapper.readValue(resourceAsInputStream(stateBeforePlayerTwoWin), MancalaGame.class);
        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.IN_PROGRESS, 12);

        gameWinnerRule.executeRule(gamePlay, game, mancalaRepository);

        assertThat(game.getWinner()).isNotNull();
        assertThat(game.getGamePlayStatus()).isEqualByComparingTo(GameStatus.FINISHED);
    }

    @Test
    void GameWinnerRule_WhenPlayerStonesEqual_GameIsDrawn() throws Exception {
        MancalaGame game = mapper.readValue(resourceAsInputStream(stateBeforeDraw), MancalaGame.class);
        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.IN_PROGRESS, 4);

        gameWinnerRule.executeRule(gamePlay, game, mancalaRepository);
        int playerOneHouseStoneCount = game.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
        int playerTwoHouseStoneCount = game.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
        int playerStoneCountIfGameDrawn = MancalaConstants.TOTAL_NUMBER_OF_STONES/2;

        assertThat(game.getWinner()).isEqualByComparingTo(GameWinner.DRAW);
        assertThat(playerOneHouseStoneCount).isEqualTo(playerStoneCountIfGameDrawn);
        assertThat(playerTwoHouseStoneCount).isEqualTo(playerStoneCountIfGameDrawn);
        assertThat(game.getGamePlayStatus()).isEqualByComparingTo(GameStatus.FINISHED);
    }
}
