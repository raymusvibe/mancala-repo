package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;

/**
 * Rule used sow stones based on the selected container.
 */
public class StoneSowingRule extends GameRule {
    @Override
    public final void executeRule(GamePlay gamePlay,
                                  MancalaGame game,
                                  MancalaRepository mancalaRepository) throws Exception {
        int containerIndex = gamePlay.getSelectedStoneContainerIndex();
        game.setSelectedStoneContainerIndex(containerIndex);
        MancalaGame gameAfterSowing = sow(game);
        mancalaRepository.saveGame(gameAfterSowing);
        successor.executeRule(gamePlay, game, mancalaRepository);
    }

    /**
     * This method sows stones on the Mancala board.
     * @param game Mancala game object from the database
     * @return MancalaGame the result of the sowing
     */
    private MancalaGame sow (MancalaGame game) {
        int selectedContainerIndex = game.getSelectedStoneContainerIndex();
        int stoneCount = game.getStoneContainer(selectedContainerIndex).getAllStonesAndEmptyContainer();

        //start adding stones to the first container after the one selected
        int targetContainerIndex = (selectedContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        //distribute stones to containers based on game rules
        while (stoneCount > MancalaConstants.EMPTY_STONE_COUNT) {
            StoneContainer targetContainer = nextContainerSowed(game, targetContainerIndex);
            int oppositeContainerIndex = MancalaConstants.PLAYER_TWO_HOUSE_INDEX - targetContainer.getMancalaGameIndex() - 1;
            //placing the last stone in empty container when opposite container still has stones, must not be my container to steal
            if (stoneCount == MancalaConstants.LAST_STONE_COUNT) {
                if (targetContainer.isEmpty()
                        && targetContainer.getMancalaGameIndex() != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                        && targetContainer.getMancalaGameIndex() != MancalaConstants.PLAYER_TWO_HOUSE_INDEX
                        && isOpponentsContainer(game.getActivePlayer(), oppositeContainerIndex)) {
                    steal(game, oppositeContainerIndex);
                } else {
                    game.getStoneContainer(targetContainer.getMancalaGameIndex()).addStone();
                }
            } else {
                game.getStoneContainer(targetContainer.getMancalaGameIndex()).addStone();
                targetContainerIndex = (targetContainer.getMancalaGameIndex() + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
            }
            stoneCount--;
        }
        changeTurn(game, targetContainerIndex);
        return game;
    }

    /**
     * This method determines which container to sow into next.
     * @param gameFromStore Mancala game object from the database
     * @param currentIndex the current game/sowing index
     * @return StoneContainer the next container to sow into
     */
    private StoneContainer nextContainerSowed(MancalaGame gameFromStore, Integer currentIndex) {
        int nextIndex = currentIndex;
        //Cannot place stones on another player's house
        if (isOpponentHouse(gameFromStore.getActivePlayer(), nextIndex)) {
            //To avoid array out of bounds error
            nextIndex = (nextIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }
        return gameFromStore.getMancalaBoard().get(nextIndex);
    }

    /**
     * The method determines whether a target container is the opposing player's house
     * @param activePlayer the current player who has just made a game move
     * @param containerIndex the current game/sowing index
     * @return boolean, is true if the container is the opponent's house
     */
    private boolean isOpponentHouse (Player activePlayer, Integer containerIndex) {
        boolean isOpponentHouse = false;
        if (activePlayer == Player.PLAYER_ONE) {
            if (containerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                isOpponentHouse = true;
            }
        } else {
            if (containerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isOpponentHouse = true;
            }
        }
        return isOpponentHouse;
    }

    /**
     * The method determines if the target container belows to the opponent (of the active player)
     * @param activePlayer the current player (who has just made a game move)
     * @param containerIndex the current game/sowing index
     * @return boolean, is true if the container belongs to opponent
     */
    private boolean isOpponentsContainer(Player activePlayer, int containerIndex) {
        boolean isNotMyContainer = false;
        if (activePlayer == Player.PLAYER_ONE) {
            if (containerIndex > MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isNotMyContainer = true;
            }
        } else {
            if (containerIndex <= MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isNotMyContainer = true;
            }
        }
        return isNotMyContainer;
    }

    /**
     * This method changes the turn between playerOne and playerTwo.
     * Turn changes occur when a player doesn't place last stone in their own house
     * @param game the Mancala game object from the repository
     * @param lastSownContainerIndex the container index of pot last sown
     */
    private void changeTurn (MancalaGame game, int lastSownContainerIndex) {
        if (game.getActivePlayer() == Player.PLAYER_ONE && lastSownContainerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || game.getActivePlayer() == Player.PLAYER_TWO && lastSownContainerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            Player newPlayer = (game.getActivePlayer() == Player.PLAYER_ONE)? Player.PLAYER_TWO : Player.PLAYER_ONE;
            game.setActivePlayer(newPlayer);
        }
    }

    /**
     * Method to steal opposing player's stones when sowing the last stone into
     * your own pot. If opposite pot is empty, then sow the last stone into players Mancala.
     * @param gameFromStore game object from client
     * @param oppositeContainerIndex the index for the opposite pot
     */
    private void steal (MancalaGame gameFromStore, Integer oppositeContainerIndex) {
        StoneContainer oppositeContainer = gameFromStore
                .getStoneContainer(oppositeContainerIndex);
        int houseIndex = (gameFromStore.getActivePlayer() == Player.PLAYER_ONE)?
                MancalaConstants.PLAYER_ONE_HOUSE_INDEX : MancalaConstants.PLAYER_TWO_HOUSE_INDEX;
        if (!oppositeContainer.isEmpty()) {
            int oppositeStones = gameFromStore
                    .getStoneContainer(oppositeContainerIndex).getAllStonesAndEmptyContainer();
            gameFromStore.getStoneContainer(houseIndex).addStones(oppositeStones + 1);
        } else {
            gameFromStore.getStoneContainer(houseIndex).addStone();
        }
    }
}
