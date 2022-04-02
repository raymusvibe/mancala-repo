package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to create a new game in case players want to restart the same game.
 */
public class NewGameRequestRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                                     MancalaGame gameFromStore,
                                     MancalaRepository mancalaRepository) throws ValidationException {
        if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.NEW) {
            assert gameFromStore != null;
            gameFromStore.initialiseBoardToNewGame();
            gameFromStore.setGamePlayStatus(GameStatus.NEW);
            gameFromStore.setActivePlayer(Player.PLAYER_TWO);
            gameFromStore.setWinner(null);
            gameFromStore.setSelectedStoneContainerIndex(MancalaConstants.PLAYER_ONE_HOUSE_INDEX);
            mancalaRepository.saveGame(gameFromStore);
        } else {
            successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaRepository);
        }
    }
}
