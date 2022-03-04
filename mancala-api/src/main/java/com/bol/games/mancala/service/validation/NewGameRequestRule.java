package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.validation.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * Rule used to create a new game in case players want to restart the same game.
 */
public class NewGameRequestRule extends Rule {
    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               Optional<MancalaGame> gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.New) {
            gameFromStore.get().initialiseBoard();
            gameFromStore.get().setActivePlayer(Player.PlayerTwo);
            gameFromStore.get().setWinner(null);
            gameFromStore.get().setSelectedStoneContainerIndex(null);
            mancalaGamesMongoTemplate.save(gameFromStore.get());
        } else {
            successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
        }
    }
}
