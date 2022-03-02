package com.bol.games.mancala.services;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.MancalaService;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MancalaServiceTests {

    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    private MancalaAPI mancalaService;

    @BeforeEach
    public void setup() {
        mancalaService = new MancalaService(mancalaGamesMongoTemplate, mancalaEventsMongoTemplate);
    }

    String invalidGameId = "someGameId";

    @Test
    public void testGameCreation () throws Exception {

        MancalaGame game = (MancalaGame) mancalaService.createGame();
        assertThat(game.getGameId()).isNotNull();
        assertThat(game.getWinner()).isNull();
        assertThat(game.getGamePlayStatus()).isEqualTo(GameStatus.NEW);
        assertThat(game.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(game.getSelectedStoneContainerIndex()).isNull();

        for (int i = 0; i < game.getMancalaBoard().size(); i++) {
            if (i != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                    && i != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                assertThat(game.getStoneContainer(i).getStones())
                        .isEqualTo(MancalaConstants.STONES_PER_CONTAINER);
            } else {
                //house containers are empty
                assertThat(game.getStoneContainer(i).isEmpty()).isEqualTo(true);
            }
        }
    }

    @Test
    public void testGameConnection () throws Exception {
        MancalaGame expectedGame = new MancalaGame();
        doReturn(expectedGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), Mockito.any(Class.class));

        MancalaGame game = (MancalaGame) mancalaService.connectToGame(expectedGame.getGameId());
        assertThat(game.getGamePlayStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(game.getGameId()).isEqualTo(expectedGame.getGameId());
        assertThat(game.getWinner()).isNull();
    }

    @Test
    public void testGameConnectionInvalidGameId () throws Exception {
        doReturn(null).when(mancalaGamesMongoTemplate).findOne(any(Query.class), Mockito.any(Class.class));

        assertThrows(NotFoundException.class, () -> {
            mancalaService.connectToGame(invalidGameId);
        }, "NotFoundException was expected");
    }
}
