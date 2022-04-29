package com.bol.games.mancala.service;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.repository.abstractions.MancalaRepositoryAPI;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MancalaServiceIntegrationTests {

    private MancalaAPI mancalaService;
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    private static final String invalidGameId = "some_invalid_game_id";
    private static final MancalaGame newGame = new MancalaGame();

    @BeforeAll
    public static void baseSetUp () {
        newGame.initialiseBoardToStartNewGame();
        newGame.setGamePlayStatus(GameStatus.IN_PROGRESS);
    }

    @BeforeEach
    public void setUp () {
        MancalaRepositoryAPI mancalaRepository = new MancalaRepository(mancalaGamesMongoTemplate, mancalaEventsMongoTemplate);
        mancalaService = new MancalaService(mancalaRepository);
    }

    @Test
    void MancalaService_WhenNewGameRequest_CreatesGame () {
        doReturn(newGame).when(mancalaGamesMongoTemplate).insert(any(MancalaGame.class));
        MancalaGame game = mancalaService.createGame();
        assertThat(game.getWinner()).isNull();
        assertThat(game.getGamePlayStatus()).isEqualTo(GameStatus.NEW);
        assertThat(game.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void MancalaService_WhenNewGameConnectionRequest_ConnectsToGame () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());
        MancalaGame game = mancalaService.connectToGame(newGame.getGameId());
        assertThat(game.getGamePlayStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void MancalaService_WhenInvalidGameId_NotFoundException () {
        assertThrows(NotFoundException.class, () -> mancalaService.connectToGame(invalidGameId),
                "NotFoundException was expected");
    }
}
