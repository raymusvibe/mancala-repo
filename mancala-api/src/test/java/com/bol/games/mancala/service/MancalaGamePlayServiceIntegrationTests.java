package com.bol.games.mancala.service;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static com.bol.games.mancala.util.ResourceReader.resourceAsInputStream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MancalaGamePlayServiceIntegrationTests {
    private MancalaGamePlayService gamePlayService;
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    private final MancalaGame newGame = new MancalaGame();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource repoStateBeforePlayerTwoWin = new ClassPathResource("gameStateBeforePlayerTwoWin.json");
    private final Resource repoStateBeforePlayerTwoOppositeStoneCapture = new ClassPathResource("gameStateBeforePlayerTwoOppositeStoneCapture.json");

    @BeforeEach
    public void setUp () {
        MancalaRepository mancalaRepository = new MancalaRepository(mancalaGamesMongoTemplate, mancalaEventsMongoTemplate);
        gamePlayService = new MancalaGamePlayService(mancalaRepository);
        newGame.initialiseBoardToStartNewGame();
        newGame.setGamePlayStatus(GameStatus.IN_PROGRESS);
    }

    @Test
    void ValidationService_WhenHouseIndexSelected_NoFurtherActionRequired () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        GamePlay gamePlay = new GamePlay(newGame.getGameId(), GameStatus.IN_PROGRESS, MancalaConstants.PLAYER_ONE_HOUSE_INDEX);
        MancalaGame updatedGame = gamePlayService.executeGameRules(gamePlay);

        assertThat(updatedGame.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(updatedGame.getWinner()).isNull();
    }

    @Test
    void ValidationService_WhenLastStoneInHouse_PlayerPlaysAgain () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        GamePlay gamePlay = new GamePlay(newGame.getGameId(), GameStatus.IN_PROGRESS, 0);
        MancalaGame updatedGame = gamePlayService.executeGameRules(gamePlay);

        assertThat(updatedGame.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void ValidationService_WhenLastStoneNotInHouse_TurnChanges () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        GamePlay gamePlay = new GamePlay(newGame.getGameId(), GameStatus.IN_PROGRESS, 1);
        MancalaGame updatedGame = gamePlayService.executeGameRules(gamePlay);

        assertThat(updatedGame.getActivePlayer()).isNotEqualByComparingTo(Player.PLAYER_ONE);
    }

    @Test
    void ValidationService_WhenPlayerMakesWinningMove_GameIsFinished () throws Exception {
        MancalaGame gameStateBeforePlayerTwoWin = mapper.readValue(resourceAsInputStream(repoStateBeforePlayerTwoWin), MancalaGame.class);
        doReturn(gameStateBeforePlayerTwoWin).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        GamePlay gamePlay = new GamePlay(gameStateBeforePlayerTwoWin.getGameId(), GameStatus.IN_PROGRESS, 12);
        MancalaGame updatedGame = gamePlayService.executeGameRules(gamePlay);

        assertThat(updatedGame.getWinner()).isNotNull();
        assertThat(updatedGame.getGamePlayStatus()).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void ValidationService_WhenOppositeStoneCaptured_TurnChanges () throws Exception {
        MancalaGame gameStateBeforePlayerTwoOppositeStoneCapture = mapper.readValue(resourceAsInputStream(repoStateBeforePlayerTwoOppositeStoneCapture), MancalaGame.class);
        doReturn(gameStateBeforePlayerTwoOppositeStoneCapture).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        GamePlay gamePlay = new GamePlay(gameStateBeforePlayerTwoOppositeStoneCapture.getGameId(), GameStatus.IN_PROGRESS, 11);
        MancalaGame updatedGame = gamePlayService.executeGameRules(gamePlay);

        assertThat(updatedGame.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(updatedGame.getWinner()).isNull();
    }
}
