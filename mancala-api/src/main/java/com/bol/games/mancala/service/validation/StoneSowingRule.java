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
 * previous state of the game stored in the database and validates
 * that against the input from the frontend.
 */
public class StoneSowingRule extends GameRule {

    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MancalaRepository mancalaRepository) throws Exception {
        assert gameFromStore != null;
        if (gameFromFrontEnd.getActivePlayer() != gameFromStore.getActivePlayer()) {
            throw new ValidationException("You cannot sow stones out of turn");
        }
        int containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        gameFromStore.setSelectedStoneContainerIndex(containerIndex);
        MancalaGame simulationResult = simulateGamePlayAgainstStoreGame(gameFromStore);
        validateMancalaBoard(gameFromFrontEnd, simulationResult);
        mancalaRepository.saveGame(simulationResult);
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaRepository);
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
        //distribute stones to containers based on game rules
        while (stoneCount > MancalaConstants.EMPTY_STONE_COUNT) {
            StoneContainer targetContainer = nextContainerSowed(gameFromStore, targetContainerIndex);
            int oppositeContainerIndex = MancalaConstants.PLAYER_TWO_HOUSE_INDEX - targetContainer.getMancalaGameIndex() - 1;
            //placing the last stone in empty container when opposite container still has stones, must not be my container to steal
            if (stoneCount == MancalaConstants.LAST_STONE_COUNT) {
                if (targetContainer.isEmpty()
                        && targetContainer.getMancalaGameIndex() != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                        && targetContainer.getMancalaGameIndex() != MancalaConstants.PLAYER_TWO_HOUSE_INDEX
                        && isNotActivePlayersContainer(gameFromStore.getActivePlayer(), oppositeContainerIndex)) {
                    steal(gameFromStore, oppositeContainerIndex);
                } else {
                    gameFromStore.getStoneContainer(targetContainer.getMancalaGameIndex()).addStone();
                }
            } else {
                gameFromStore.getStoneContainer(targetContainer.getMancalaGameIndex()).addStone();
                targetContainerIndex = (targetContainer.getMancalaGameIndex() + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
            }
            stoneCount--;
        }
        changeTurn(gameFromStore, targetContainerIndex);
        return gameFromStore;
    }

    /*
     * TODO
     * */
    private StoneContainer nextContainerSowed(MancalaGame gameFromStore, Integer currentIndex) {
        int nextIndex = currentIndex;
        //cannot place stones on another player's house
        if (isOpponentHouse(gameFromStore.getActivePlayer(), nextIndex)) {
            //avoid array out of bounds error
            nextIndex = (nextIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }
        return gameFromStore.getMancalaBoard().get(nextIndex);
    }

    /*
     * TODO
     * */
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

    /*
    * TODO
    * */
    private boolean isNotActivePlayersContainer(Player activePlayer, int targetContainerIndex) {
        boolean isNotMyContainer = false;
        if (activePlayer == Player.PLAYER_ONE) {
            if (targetContainerIndex > MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isNotMyContainer = true;
            }
        } else {
            if (targetContainerIndex <= MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isNotMyContainer = true;
            }
        }
        return isNotMyContainer;
    }

    /**
     * This method changes the turn between playerOne and playerTwo.
     * This occurs when you don't place last stone in your own house
     * @param gameFromStore game object from client
     * @param lastSownContainerIndex the container index of pot last sown
     */
    private void changeTurn (MancalaGame gameFromStore, int lastSownContainerIndex) {
        if (gameFromStore.getActivePlayer() == Player.PLAYER_ONE && lastSownContainerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || gameFromStore.getActivePlayer() == Player.PLAYER_TWO && lastSownContainerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            Player newPlayer = (gameFromStore.getActivePlayer() == Player.PLAYER_ONE)? Player.PLAYER_TWO : Player.PLAYER_ONE;
            gameFromStore.setActivePlayer(newPlayer);
        }
    }

    /**
     * Method to steal opposing player's stones when sowing the last stone into
     * your own pot. If opposite pot is empty, then sow the last stone into your Mancala.
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

    /**
     * This method validates the Mancala board from the frontend against the simulation.
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
}
