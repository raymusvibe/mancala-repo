package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to validate the sowing of stones done in the frontend.
 * It runs a simulation using the selected container index and the
 * previous state of the game stored in the database and compares that
 * with the input from the frontend.
 */
public class StoneSowingRule extends GameRule {

    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MancalaRepository mancalaRepository) throws ValidationException {

        assert gameFromStore != null;
        if (gameFromFrontEnd.getActivePlayer() != gameFromStore.getActivePlayer()) {
            throw new ValidationException("You cannot sow stones out of turn");
        }
        Integer containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        gameFromStore.setSelectedStoneContainerIndex(containerIndex);
        MancalaGame simulationResult = simulateGamePlayAgainstStoreGame(gameFromStore);
        validateMancalaBoard(gameFromFrontEnd, simulationResult);
        mancalaRepository.saveGame(simulationResult);
    }

    /**
     * This method validates the input from the frontend against the simulation.
     * @param gameFromFrontEnd game from the frontend
     * @param simulationResult  result of the simulation
     */
    private void validateMancalaBoard(MancalaGame gameFromFrontEnd,
                                      MancalaGame simulationResult) throws ValidationException {
        for (int i = 0; i < gameFromFrontEnd.getMancalaBoard().size(); i++) {
            if (gameFromFrontEnd.getStoneContainer(i).getStones() != simulationResult.getStoneContainer(i).getStones()) {
                throw new ValidationException("Error validating stone containers from client");
            }
        }
    }

    /**
     * This method simulates the stone sowing done in the frontend.
     * @param gameFromStore game from the database to simulate stone sowing on
     * @return MancalaGame the result of the simulation
     */
    private MancalaGame simulateGamePlayAgainstStoreGame(MancalaGame gameFromStore) {
        int selectedContainerIndex = gameFromStore.getSelectedStoneContainerIndex();
        int stoneCount = gameFromStore.getStoneContainer(selectedContainerIndex).getAllStonesAndEmptyContainer();

        //start adding stones to the first container after the one selected
        int containerIndex = (selectedContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        Player activePlayer = gameFromStore.getActivePlayer();

        //distribute stones to containers based on game rules
        while (stoneCount > MancalaConstants.EMPTY_STONE_COUNT) {
            //cannot place stones on another player's house
            if (activePlayer == Player.PLAYER_ONE && containerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX
                    || activePlayer == Player.PLAYER_TWO && containerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                containerIndex = (containerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
                continue;
            }
            StoneContainer targetContainer = gameFromStore.getStoneContainer(containerIndex);
            //placing the last stone in empty container when opposite container still has stones
            if (stoneCount == MancalaConstants.LAST_STONE_COUNT) {
                if (targetContainer.isEmpty()
                        && containerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                        && containerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                    StoneContainer oppositeContainer = gameFromStore
                            .getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX - containerIndex - 1);
                    if (!oppositeContainer.isEmpty()) {
                        int houseIndex = (activePlayer == Player.PLAYER_ONE)?
                                MancalaConstants.PLAYER_ONE_HOUSE_INDEX : MancalaConstants.PLAYER_TWO_HOUSE_INDEX;
                        int oppositeStones = oppositeContainer.getAllStonesAndEmptyContainer();
                        gameFromStore.getStoneContainer(houseIndex).addStones(oppositeStones + 1);
                        break;
                    }
                }
                targetContainer.addStone();
                break;
            }
            targetContainer.addStone();
            stoneCount--;
            containerIndex = (containerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }

        //change player if you didn't place last stone in your own house, assisted by logic above
        if (activePlayer == Player.PLAYER_ONE && containerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || activePlayer == Player.PLAYER_TWO && containerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            gameFromStore.setActivePlayer(changePlayer(activePlayer));
        }
        return gameFromStore;
    }

    /**
     * This method changes the turn between playerOne and playerTwo.
     * @param currentPlayer player who has just made a move
     * @return Player turn switches to other player
     */
    private Player changePlayer (Player currentPlayer) {
        return (currentPlayer == Player.PLAYER_ONE)? Player.PLAYER_TWO : Player.PLAYER_ONE;
    }
}
