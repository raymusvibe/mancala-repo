package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validation.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * Rule used to validate the sowing of stones and the state of the board after sowing in the frontend.
 * It runs a simulation using the selected container index and the previous state of the game stored in the database
 * and compares that with the input from the frontend.
 */
public class StoneSowingRule extends Rule {

    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               Optional<MancalaGame> gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {

        if (gameFromFrontEnd.getActivePlayer() != gameFromStore.get().getActivePlayer()) {
            throw new ValidationException("You cannot sow stones out of turn");
        }
        Integer containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        gameFromStore.get().setSelectedStoneContainerIndex(containerIndex);
        MancalaGame simulatedResultFromStore = simulateGamePlayOnStoreGame(gameFromStore.get());
        validateMancalaBoard(gameFromFrontEnd, simulatedResultFromStore);
        mancalaGamesMongoTemplate.save(simulatedResultFromStore);
    }

    private void validateMancalaBoard(MancalaGame gameFromFrontEnd,
                                      MancalaGame simulatedResultFromStore) throws ValidationException {
        for (int i = 0; i < gameFromFrontEnd.getMancalaBoard().size(); i++) {
            if (gameFromFrontEnd.getStoneContainer(i).getStones() != simulatedResultFromStore.getStoneContainer(i).getStones()) {
                throw new ValidationException("Error validating stone containers from client");
            }
        }
    }

    private MancalaGame simulateGamePlayOnStoreGame(MancalaGame gameFromStore) {
        int selectedContainerIndex = gameFromStore.getSelectedStoneContainerIndex();
        int stoneCount = gameFromStore.getStoneContainer(selectedContainerIndex).getAllStonesAndEmptyContainer();

        //start adding stones to the first container after the one selected
        int currentContainerIndex = (selectedContainerIndex + 1) % (MancalaConstants.PlayerTwoHouseIndex + 1);
        Player activePlayer = gameFromStore.getActivePlayer();

        //distribute stones to containers based on game rules
        while (stoneCount > 0) {
            //cannot place stones on another player's house
            if (activePlayer == Player.PlayerOne && currentContainerIndex == MancalaConstants.PlayerTwoHouseIndex
                    || activePlayer == Player.PlayerTwo && currentContainerIndex == MancalaConstants.PlayerOneHouseIndex) {
                currentContainerIndex = (currentContainerIndex + 1) % (MancalaConstants.PlayerTwoHouseIndex + 1);
                continue;
            }
            StoneContainer targetContainer = gameFromStore.getStoneContainer(currentContainerIndex);
            //placing the last stone in empty container when opposite container still has stones
            if (stoneCount == 1) {
                if (targetContainer.isEmpty()
                        && currentContainerIndex != MancalaConstants.PlayerOneHouseIndex
                        && currentContainerIndex != MancalaConstants.PlayerTwoHouseIndex) {
                    StoneContainer oppositeContainer = gameFromStore
                            .getStoneContainer(MancalaConstants.PlayerTwoHouseIndex - currentContainerIndex - 1);
                    if (!oppositeContainer.isEmpty()) {
                        int houseIndex = (activePlayer == Player.PlayerOne)?
                                MancalaConstants.PlayerOneHouseIndex : MancalaConstants.PlayerTwoHouseIndex;
                        int stonesFromOppositeContainer = oppositeContainer.getAllStonesAndEmptyContainer();
                        gameFromStore.getStoneContainer(houseIndex).addStones(stonesFromOppositeContainer + 1);
                        stoneCount--;
                        break;
                    }
                }
                targetContainer.addStone();
                stoneCount--;
                break;
            }
            targetContainer.addStone();
            stoneCount--;
            currentContainerIndex = (currentContainerIndex + 1) % (MancalaConstants.PlayerTwoHouseIndex + 1);
        }

        //change player if you didn't place last stone in your own house, assisted by logic above
        if (activePlayer == Player.PlayerOne && currentContainerIndex != MancalaConstants.PlayerOneHouseIndex
                || activePlayer == Player.PlayerTwo && currentContainerIndex != MancalaConstants.PlayerTwoHouseIndex) {
            gameFromStore.setActivePlayer(changePlayer(activePlayer));
        }
        return gameFromStore;
    }

    private Player changePlayer (Player currentPlayer) {
        if (currentPlayer == Player.PlayerOne) {
            return Player.PlayerTwo;
        }
        return Player.PlayerOne;
    }
}
