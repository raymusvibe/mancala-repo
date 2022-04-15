package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to restart a game. The rule simply updates the game object in the
 * store and returns if a restart request is received. The game can only transition
 * to a restarting status from a finished status.
 */
public class GameRestartRequestRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                                     MancalaGame gameFromStore,
                                     MancalaRepository mancalaRepository) throws Exception {
        assert gameFromStore != null;
        if (isInvalidRestartStatusChange(gameFromFrontEnd.getGamePlayStatus(), gameFromStore.getGamePlayStatus())) {
            throw new ValidationException("Invalid game status change");
        } else if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.RESTARTING
                && gameFromStore.getGamePlayStatus() == GameStatus.FINISHED) {
            gameFromStore.initialiseBoardToNewGame();
            gameFromStore.setGamePlayStatus(GameStatus.RESTARTING);
            gameFromStore.setActivePlayer(Player.PLAYER_TWO);
            gameFromStore.setWinner(null);
            gameFromStore.setSelectedStoneContainerIndex(MancalaConstants.PLAYER_ONE_HOUSE_INDEX);
            mancalaRepository.saveGame(gameFromStore);
        } else {
            //Players have restarted the game and moved on from the RESTARTING status, update store
            if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.IN_PROGRESS
                    && gameFromStore.getGamePlayStatus() == GameStatus.RESTARTING) {
                gameFromStore.setGamePlayStatus(GameStatus.IN_PROGRESS);
                mancalaRepository.saveGame(gameFromStore);
            }
            successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaRepository);
        }
    }

    private boolean isInvalidRestartStatusChange(GameStatus gameFromFrontEndStatus, GameStatus gameFromStoreStatus) {
        return gameFromFrontEndStatus == GameStatus.RESTARTING
                && gameFromStoreStatus != GameStatus.FINISHED ||
                gameFromStoreStatus == GameStatus.RESTARTING
                && gameFromFrontEndStatus != GameStatus.IN_PROGRESS;
    }
}
