package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to validate the sowing of stones done in the frontend.
 * It runs a simulation using the selected container index and the
 * previous state of the game stored in the database and validates
 * that against the input from the frontend.
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
        int containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        gameFromStore.setSelectedStoneContainerIndex(containerIndex);
        MancalaGame simulationResult = simulateGamePlayAgainstStoreGame(gameFromStore);
        validateMancalaBoard(gameFromFrontEnd, simulationResult);
        if (simulationResult.getGamePlayStatus() != GameStatus.IN_PROGRESS) {
            simulationResult.setGamePlayStatus(GameStatus.IN_PROGRESS);
        }
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
        int targetContainerIndex = (selectedContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        Player activePlayer = gameFromStore.getActivePlayer();

        //distribute stones to containers based on game rules
        while (stoneCount > MancalaConstants.EMPTY_STONE_COUNT) {
            StoneContainer targetContainer = gameFromStore.getNextContainerSowed(targetContainerIndex);
            int oppositeContainerIndex = MancalaConstants.PLAYER_TWO_HOUSE_INDEX - targetContainer.getMancalaGameIndex() - 1;
            //placing the last stone in empty container when opposite container still has stones, must not be my container to steal
            if (stoneCount == MancalaConstants.LAST_STONE_COUNT) {
                if (targetContainer.isEmpty()
                        && targetContainer.getMancalaGameIndex() != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                        && targetContainer.getMancalaGameIndex() != MancalaConstants.PLAYER_TWO_HOUSE_INDEX
                        && gameFromStore.isNotMyContainer(oppositeContainerIndex)) {
                    StoneContainer oppositeContainer = gameFromStore
                            .getStoneContainer(oppositeContainerIndex);
                    int houseIndex = (activePlayer == Player.PLAYER_ONE)?
                            MancalaConstants.PLAYER_ONE_HOUSE_INDEX : MancalaConstants.PLAYER_TWO_HOUSE_INDEX;
                    if (!oppositeContainer.isEmpty()) {
                        int oppositeStones = gameFromStore
                                .getStoneContainer(oppositeContainerIndex).getAllStonesAndEmptyContainer();
                        gameFromStore.getStoneContainer(houseIndex).addStones(oppositeStones + 1);
                    } else {
                        gameFromStore.getStoneContainer(houseIndex).addStone();
                    }
                    break;
                }
                gameFromStore.getStoneContainer(targetContainer.getMancalaGameIndex()).addStone();
                break;
            }
            gameFromStore.getStoneContainer(targetContainer.getMancalaGameIndex()).addStone();
            stoneCount--;
            targetContainerIndex = (targetContainer.getMancalaGameIndex() + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }

        //change player if you didn't place last stone in your own house, assisted by logic above that skips placing stone in opponent house
        if (activePlayer == Player.PLAYER_ONE && targetContainerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || activePlayer == Player.PLAYER_TWO && targetContainerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
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
