package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * Rule used to create a new game in case players want to restart the same game.
 */
public class NewGameRequestRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                               Optional<MancalaGame> gameFromStore,
                               MongoTemplate mongoTemplate) throws ValidationException {
        if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.NEW) {
            MancalaGame storeGame = gameFromStore.get();
            storeGame.initialiseBoard();
            storeGame.setActivePlayer(Player.PLAYER_TWO);
            storeGame.setWinner(null);
            storeGame.setSelectedStoneContainerIndex(null);
            mongoTemplate.save(storeGame);
        } else {
            successor.processRequest(gameFromFrontEnd, gameFromStore, mongoTemplate);
        }
    }
}
