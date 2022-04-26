package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;
import org.springframework.stereotype.Component;

/**
 * Rule used to sow stones based on the selected container.
 */
@Component
public class StoneSowingRule extends GameRule {
    @Override
    public final void executeRule(GamePlay gamePlay,
                                  MancalaGame game,
                                  MancalaRepository mancalaRepository) throws ValidationException {
        int containerIndex = gamePlay.getSelectedStoneContainerIndex();
        game.setSelectedStoneContainerIndex(containerIndex);
        sow(game);
        mancalaRepository.saveGame(game);
        successor.executeRule(gamePlay, game, mancalaRepository);
    }

    /**
     * This method sows stones on the Mancala board. When placing the
     * last stone in empty container and the opposite container still
     * has stones, a player can steal from the opposite container.
     * @param game Mancala game object from the database
     */
    private void sow (MancalaGame game) {
        int selectedContainerIndex = game.getSelectedStoneContainerIndex();
        int stoneCount = game.getStoneContainer(selectedContainerIndex).getAllStonesAndEmptyContainer();

        int targetContainerIndex = (selectedContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        while (stoneCount > MancalaConstants.EMPTY_STONE_COUNT) {
            StoneContainer targetContainer = nextContainerSowed(game, targetContainerIndex);
            int oppositeContainerIndex = MancalaConstants.PLAYER_TWO_HOUSE_INDEX - targetContainer.getMancalaGameIndex() - 1;
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
    }

    /**
     * This method determines which container to sow into next.
     * A player cannot place stones on another player's house.
     * @param game Mancala game object from the database
     * @param currentIndex the current game/sowing index
     * @return StoneContainer the next container to sow into
     */
    private StoneContainer nextContainerSowed(MancalaGame game, Integer currentIndex) {
        int nextIndex = currentIndex;
        if (isOpponentHouse(game.getActivePlayer(), nextIndex)) {
            nextIndex = (nextIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }
        return game.getMancalaBoard().get(nextIndex);
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
     * The method determines if the target container belongs to the opponent (of the active player)
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
     * Method to steal opposing player's stones when sowing the last stone into your own empty pot.
     * If opposite pot is empty, then just sow the last stone into player's Mancala.
     * @param game game object from repo
     * @param oppositeContainerIndex the index for the opposite pot
     */
    private void steal (MancalaGame game, Integer oppositeContainerIndex) {
        StoneContainer oppositeContainer = game
                .getStoneContainer(oppositeContainerIndex);
        int houseIndex = (game.getActivePlayer() == Player.PLAYER_ONE)?
                MancalaConstants.PLAYER_ONE_HOUSE_INDEX : MancalaConstants.PLAYER_TWO_HOUSE_INDEX;
        if (!oppositeContainer.isEmpty()) {
            int oppositeStones = game
                    .getStoneContainer(oppositeContainerIndex).getAllStonesAndEmptyContainer();
            game.getStoneContainer(houseIndex).addStones(oppositeStones + 1);
        } else {
            game.getStoneContainer(houseIndex).addStone();
        }
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
}
