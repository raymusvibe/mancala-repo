package com.bol.games.mancala.service;

import com.bol.games.mancala.exception.*;
import com.bol.games.mancala.model.*;
import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.service.abstractions.MancalaGameValidationAPI;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MancalaGameValidationService implements MancalaGameValidationAPI {

    private MongoTemplate mancalaGamesMongoTemplate;
    private MongoTemplate mancalaEventsMongoTemplate;

    @Override
    public MancalaGame validate(MancalaGame gameFromFrontEnd) throws ValidationException {
        mancalaEventsMongoTemplate.insert(gameFromFrontEnd);
        //validate game id
        MancalaGame gameFromStore = findGameInStore(gameFromFrontEnd.getGameId());
        if (gameFromStore == null) {
            throw new ValidationException("Invalid game Id provided: " + gameFromFrontEnd.getGameId());
        }

        //players want to play a new game on the same gameId
        if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.NEW ) {
            gameFromFrontEnd.initialiseBoard();
            gameFromFrontEnd.setActivePlayer(Player.PLAYER_TWO);
            gameFromFrontEnd.setWinner(null);
            gameFromFrontEnd.setSelectedStoneContainerIndex(null);
            mancalaGamesMongoTemplate.save(gameFromFrontEnd);
            return gameFromFrontEnd;
        }

        //first check if there is a winner
        if (gameFromFrontEnd.getWinner() != null) {
            if (validateWinner (gameFromFrontEnd)) {
                gameFromStore.setWinner(gameFromFrontEnd.getWinner());
                gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
                mancalaGamesMongoTemplate.save(gameFromStore);
                return gameFromFrontEnd;
            } else
                throw new ValidationException("Invalid game state detected (Game winner status incorrect)");
        }

        //no action required for selecting an empty container
        Integer containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        StoneContainer selectedStoneContainer = gameFromStore.getStoneContainer(containerIndex);
        if (selectedStoneContainer.isEmpty()) {
            return gameFromFrontEnd;
        }

        //cannot make a play from house containers, no action required
        if (containerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || containerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            return gameFromFrontEnd;
        }

        //validate stone count when there is no winner yet
        validateStoneCount(gameFromFrontEnd.getMancalaBoard());

        //player cannot start from opponents side
        if (gameFromFrontEnd.getActivePlayer() == Player.PLAYER_ONE && containerIndex > MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || gameFromFrontEnd.getActivePlayer() == Player.PLAYER_TWO && containerIndex < MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || gameFromFrontEnd.getActivePlayer() == Player.PLAYER_TWO && containerIndex > MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            throw new ValidationException("Invalid game state detected (Container selection index)");
        }

        //simulate stone sowing done in the front-end and validate the result
        gameFromStore.setSelectedStoneContainerIndex(containerIndex);
        MancalaGame simulatedResultFromStore = simulateGamePlay(gameFromStore);
        if (validateMancalaBoard(gameFromFrontEnd.getMancalaBoard(), simulatedResultFromStore.getMancalaBoard())) {
            mancalaGamesMongoTemplate.save(simulatedResultFromStore);
            return simulatedResultFromStore;
        } else {
            throw new ValidationException("Error validating stone containers from client");
        }
    }

    private MancalaGame findGameInStore (String gameId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameId));
        return mancalaGamesMongoTemplate.findOne(query, MancalaGame.class);
    }

    private void validateStoneCount (List<StoneContainer> boardFromFrontEnd) throws ValidationException {
        Integer sum = boardFromFrontEnd.stream()
                .map(x -> x.getStones())
                .reduce(0, Integer::sum);
        int totalNumberOfStones = MancalaConstants.CONTAINERS_PER_PLAYER * MancalaConstants.STONES_PER_CONTAINER * 2;
        if (totalNumberOfStones != sum)
            throw new ValidationException("Error validating stone count");
    }

    private boolean validateMancalaBoard(List<StoneContainer> boardFromFrontEnd, List<StoneContainer> simulatedBoardFromStore) {
        for (int i = 0; i < boardFromFrontEnd.size(); i++) {
            if (boardFromFrontEnd.get(i).getStones() != simulatedBoardFromStore.get(i).getStones()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateWinner (MancalaGame gameFromFrontEnd) throws ValidationException {
        int allocatedNumberOfStonesPerPlayer = MancalaConstants.CONTAINERS_PER_PLAYER * MancalaConstants.STONES_PER_CONTAINER;
        int playerOneStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
        int playerTwoStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
        if (playerOneStones + playerTwoStones != allocatedNumberOfStonesPerPlayer * 2)
            throw new ValidationException("Error validating stone count for winner");
        if (playerOneStones > allocatedNumberOfStonesPerPlayer) {
            //player one should be the winner in this case
            if (gameFromFrontEnd.getWinner().equals(Player.PLAYER_ONE))
                return true;
        } else {
            if (gameFromFrontEnd.getWinner().equals(Player.PLAYER_TWO))
                return true;
        }
        return false;
    }

    private MancalaGame simulateGamePlay (MancalaGame gameFromStore) {
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
        if (currentContainerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                && currentContainerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            gameFromStore.setActivePlayer(changePlayer(activePlayer));
        }
        return gameFromStore;
    }

    private Player changePlayer (Player currentPlayer) {
        if (currentPlayer == Player.PLAYER_ONE)
            return Player.PLAYER_TWO;
        return Player.PLAYER_ONE;
    }
}
