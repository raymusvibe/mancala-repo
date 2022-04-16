package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;

/**
 * Rule used to restart a game. The rule simply updates the game object in the
 * store to the RESTARTING status and returns if a RESTARTING status is received.
 * The game can only transition to a RESTARTING status from a FINISHED status.
 * And from RESTARTING it can only transition to IN_PROGRESS.
 * All other persisted game statuses are initiated by the backend.
 */
public class GameRestartRequestRule extends GameRule {
    @Override
    public final void executeRule(GamePlay gamePlay,
                                  MancalaGame game,
                                  MancalaRepository mancalaRepository) throws Exception {
        assert game != null;

        if (isInvalidRestartStatusChange(gamePlay.getGamePlayStatus(), game.getGamePlayStatus())) {
            throw new ValidationException("Invalid game status change");
        }

        if (gamePlay.getGamePlayStatus() == GameStatus.RESTARTING
                && game.getGamePlayStatus() == GameStatus.FINISHED) {
            game.initialiseBoardToStartNewGame();
            game.setGamePlayStatus(GameStatus.RESTARTING);
            game.setActivePlayer(Player.PLAYER_TWO);
            game.setWinner(null);
            game.setSelectedStoneContainerIndex(MancalaConstants.PLAYER_ONE_HOUSE_INDEX);
            mancalaRepository.saveGame(game);
        } else {
            //Players have restarted the game and moved on from the RESTARTING status, update store
            if (gamePlay.getGamePlayStatus() == GameStatus.IN_PROGRESS
                    && game.getGamePlayStatus() == GameStatus.RESTARTING) {
                game.setGamePlayStatus(GameStatus.IN_PROGRESS);
                mancalaRepository.saveGame(game);
            }
            successor.executeRule(gamePlay, game, mancalaRepository);
        }
    }

    /**
     * This method determines whether the game status change around restarts is valid or legal.
     * @param gamePlayStatus the game status from the client/frontend
     * @param gameFromStoreStatus the game status from the game object in the DB
     * @return boolean, is true if an invalid status is detected
     */
    private boolean isInvalidRestartStatusChange(GameStatus gamePlayStatus, GameStatus gameFromStoreStatus) {
        return gamePlayStatus == GameStatus.RESTARTING
                && gameFromStoreStatus != GameStatus.FINISHED ||
                gameFromStoreStatus == GameStatus.RESTARTING
                && gamePlayStatus != GameStatus.IN_PROGRESS;
    }
}
