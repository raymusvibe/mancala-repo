package com.bol.games.mancala.services;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.MancalaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private MancalaService mancalaService;
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    String invalidGameId = "someGameId";

    @Test
    public void testGameCreation () throws Exception {

        MancalaGame game = mancalaService.createGame();
        assertThat(game.getGameId()).isNotNull();
        assertThat(game.getWinner()).isNull();
        assertThat(game.getGamePlayStatus()).isEqualTo(GameStatus.New);
        assertThat(game.getActivePlayer()).isEqualTo(Player.PlayerOne);
        assertThat(game.getSelectedStoneContainerIndex()).isNull();

        for (int i = 0; i < game.getMancalaBoard().size(); i++) {
            if (i != MancalaConstants.PlayerOneHouseIndex
                    && i != MancalaConstants.PlayerTwoHouseIndex) {
                assertThat(game.getStoneContainer(i).getStones())
                        .isEqualTo(MancalaConstants.StonesPerPlayer);
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
        //Mock injections doesn't seem to work for coverage plugin
        mancalaService = new MancalaService(mancalaGamesMongoTemplate, mancalaEventsMongoTemplate);
        MancalaGame game = mancalaService.connectToGame(expectedGame.getGameId());
        assertThat(game.getGamePlayStatus()).isEqualTo(GameStatus.InProgress);
        assertThat(game.getActivePlayer()).isEqualTo(Player.PlayerOne);
        assertThat(game.getGameId()).isEqualTo(expectedGame.getGameId());
        assertThat(game.getWinner()).isNull();
    }

    @Test
    public void testGameConnectionInvalidGameId () throws Exception {
        assertThrows(NotFoundException.class, () -> {
            mancalaService.connectToGame(invalidGameId);
        }, "NotFoundException was expected");
    }
}
