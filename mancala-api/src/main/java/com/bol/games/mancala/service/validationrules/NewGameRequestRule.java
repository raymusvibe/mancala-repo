package com.bol.games.mancala.service.validationrules;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

public class NewGameRequestRule extends Rule {
    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.NEW ) {
            gameFromStore.initialiseBoard();
            gameFromStore.setActivePlayer(Player.PLAYER_TWO);
            gameFromStore.setWinner(null);
            gameFromStore.setSelectedStoneContainerIndex(null);
            mancalaGamesMongoTemplate.save(gameFromStore);
        } else {
            successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
        }
    }
}
