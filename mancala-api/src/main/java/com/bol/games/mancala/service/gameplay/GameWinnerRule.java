package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.model.GameWinner;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;

/**
 * Rule used to determine the winner.
 */
public class GameWinnerRule extends GameRule {
    @Override
    public final void executeRule(GamePlay gamePlay,
                                  MancalaGame game,
                                  MancalaRepository mancalaRepository) {
        if (isGameFinished(game)) {
            int playerOneFinalStoneCount = game.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
            int playerTwoFinalStoneCount = game.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();

            assert playerOneFinalStoneCount + playerTwoFinalStoneCount == MancalaConstants.TOTAL_NUMBER_OF_STONES;

            if (playerOneFinalStoneCount > playerTwoFinalStoneCount) {
                game.setWinner(GameWinner.PLAYER_ONE);
            } else if (playerOneFinalStoneCount < playerTwoFinalStoneCount) {
                game.setWinner(GameWinner.PLAYER_TWO);
            } else {
                game.setWinner(GameWinner.DRAW);
            }
            game.setGamePlayStatus(GameStatus.FINISHED);
            game.setSelectedStoneContainerIndex(MancalaConstants.PLAYER_ONE_HOUSE_INDEX);
            mancalaRepository.saveGame(game);
        }
    }

    /**
     * This method determines whether the game is finished or not.
     * @param game Mancala game object from the database
     * @return boolean, is true if at least one player has no more stones
     */
    private boolean isGameFinished (MancalaGame game) {
        boolean isFinished = true;
        for (int i = 0; i < MancalaConstants.PLAYER_ONE_HOUSE_INDEX; i++) {
            if (game.getStoneContainer(i).getStones() > 0) {
                isFinished = false;
                break;
            }
        }
        if (isFinished) {
            movePlayerStonesToHouse(game, Player.PLAYER_TWO);
        } else {
            isFinished = true;
            for (int i = MancalaConstants.PLAYER_ONE_HOUSE_INDEX + 1; i < MancalaConstants.PLAYER_TWO_HOUSE_INDEX; i++) {
                if (game.getStoneContainer(i).getStones() > 0) {
                    isFinished = false;
                    break;
                }
            }
            if (isFinished) {
                movePlayerStonesToHouse(game, Player.PLAYER_ONE);
            }
        }
        return isFinished;
    }

    /**
     * This method moves a player's stones to their house pot.
     * @param game Mancala game object from the database
     * @param selectedPlayer either of the players
     */
    private void movePlayerStonesToHouse (MancalaGame game, Player selectedPlayer) {
        if (selectedPlayer == Player.PLAYER_ONE) {
            for (int i = 0; i < MancalaConstants.PLAYER_ONE_HOUSE_INDEX; i++) {
                game.getMancalaBoard().get(MancalaConstants.PLAYER_ONE_HOUSE_INDEX)
                        .addStones(game.getMancalaBoard().get(i).getAllStonesAndEmptyContainer());
            }
        } else {
            for (int i = MancalaConstants.PLAYER_ONE_HOUSE_INDEX + 1; i < MancalaConstants.PLAYER_TWO_HOUSE_INDEX; i++) {
                game.getMancalaBoard().get(MancalaConstants.PLAYER_TWO_HOUSE_INDEX)
                        .addStones(game.getMancalaBoard().get(i).getAllStonesAndEmptyContainer());
            }
        }
    }
}
