package com.bol.games.mancala.service.validationrules;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

//last rule in chain
public class StoneSowingRule extends Rule {

    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        Integer containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        gameFromStore.setSelectedStoneContainerIndex(containerIndex);
        MancalaGame simulatedResultFromStore = simulateGamePlayOnStoreGame(gameFromStore);
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
        int currentContainerIndex = (selectedContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        Player activePlayer = gameFromStore.getActivePlayer();

        //distribute stones to containers based on game rules
        while (stoneCount > 0) {
            //cannot place stones on another player's house
            if (activePlayer == Player.PLAYER_ONE && currentContainerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX
                    || activePlayer == Player.PLAYER_TWO && currentContainerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                currentContainerIndex = (currentContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
                continue;
            }
            StoneContainer targetContainer = gameFromStore.getStoneContainer(currentContainerIndex);
            //placing the last stone in empty container when opposite container still has stones
            if (stoneCount == 1) {
                if (targetContainer.isEmpty()
                        && currentContainerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                        && currentContainerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                    StoneContainer oppositeContainer = gameFromStore
                            .getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX - currentContainerIndex - 1);
                    if (!oppositeContainer.isEmpty()) {
                        int houseIndex = (activePlayer == Player.PLAYER_ONE)?
                                MancalaConstants.PLAYER_ONE_HOUSE_INDEX : MancalaConstants.PLAYER_TWO_HOUSE_INDEX;
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
            currentContainerIndex = (currentContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }

        //change player if you didn't place last stone in your own house, assisted by logic above
        if (activePlayer == Player.PLAYER_ONE && currentContainerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX)
            gameFromStore.setActivePlayer(changePlayer(activePlayer));
        if (activePlayer == Player.PLAYER_TWO && currentContainerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX)
            gameFromStore.setActivePlayer(changePlayer(activePlayer));
        return gameFromStore;
    }

    private Player changePlayer (Player currentPlayer) {
        if (currentPlayer == Player.PLAYER_ONE)
            return Player.PLAYER_TWO;
        return Player.PLAYER_ONE;
    }

}
