package com.bol.games.mancala.service;

import com.bol.games.mancala.exception.*;
import com.bol.games.mancala.model.*;
import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.data.MancalaRepository;
import com.bol.games.mancala.service.abstractions.MancalaGameValidationAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MancalaGameValidationService implements MancalaGameValidationAPI {

    @Autowired
    private MancalaRepository mancalaRepository;

    @Override
    public MancalaGame validate(MancalaGame gameFromFrontEnd) throws ValidationException {

        //validate game id
        Optional<MancalaGame> mancalaFromStore = mancalaRepository.findById(gameFromFrontEnd.getGameId());
        if (!mancalaFromStore.isPresent()) {
            throw new ValidationException("Invalid game Id provided: " + gameFromFrontEnd.getGameId());
        }

        //validate game status, must not be new
        if (gameFromFrontEnd.getGamePlayStatus() == GameStatus.NEW ) {
            throw new ValidationException("Invalid game state detected (Game play status)");
        }

        //first check if there is a winner
        MancalaGame gameFromStore = mancalaFromStore.get();
        if (gameFromFrontEnd.getWinner() != null) {
            if (validateWinner (gameFromFrontEnd, gameFromStore.getPlayerOne(), gameFromStore.getPlayerTwo())) {
                gameFromStore.setWinner(gameFromFrontEnd.getWinner());
                gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
                mancalaRepository.save(gameFromStore);
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

        //player turn is controlled from the backend
        ActivePlayer playerFromFrontEnd = gameFromFrontEnd.getActivePlayer();
        ActivePlayer playerFromStore = gameFromStore.getActivePlayer();
        if (playerFromFrontEnd != playerFromStore) {
            throw new ValidationException("Invalid game state detected (Active player misalignment)");
        }

        //player cannot start from opponents side
        if (gameFromFrontEnd.getActivePlayer() == ActivePlayer.PLAYER_ONE && containerIndex > MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || gameFromFrontEnd.getActivePlayer() == ActivePlayer.PLAYER_TWO && containerIndex < MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || gameFromFrontEnd.getActivePlayer() == ActivePlayer.PLAYER_TWO && containerIndex > MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            throw new ValidationException("Invalid game state detected (Container selection index)");
        }
        //simulate stone sowing from front-end and validate the result
        gameFromStore.setSelectedStoneContainerIndex(containerIndex);
        MancalaGame simulatedResultFromStore = simulateGamePlay(gameFromStore);
        if (validateMancalaBoard(gameFromFrontEnd.getMancalaBoard(), simulatedResultFromStore.getMancalaBoard())) {
            mancalaRepository.save(simulatedResultFromStore);
            return simulatedResultFromStore;
        } else {
            throw new ValidationException("Error validating stone containers from client");
        }
    }

    private boolean validateMancalaBoard(List<StoneContainer> boardFromFrontEnd, List<StoneContainer> simulatedBoardFromStore) {
        for (int i = 0; i < boardFromFrontEnd.size(); i++) {
            if (boardFromFrontEnd.get(i).getStones() != simulatedBoardFromStore.get(i).getStones()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateWinner (MancalaGame gameFromFrontEnd, Player playerOne, Player playerTwo) throws ValidationException {
        int allocatedNumberOfStonesPerPlayer = MancalaConstants.CONTAINERS_PER_PLAYER * MancalaConstants.STONES_PER_CONTAINER;
        int playerOneStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
        int playerTwoStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
        if (playerOneStones + playerTwoStones != allocatedNumberOfStonesPerPlayer * 2)
            throw new ValidationException("Error validating stone count");
        Player winnerFromFrontEnd = gameFromFrontEnd.getWinner();
        if (playerOneStones > allocatedNumberOfStonesPerPlayer) {
            //player one should be the winner in this case
            if (winnerFromFrontEnd.getPlayerName().equals(playerOne.getPlayerName()))
                return true;
        } else {
            if (winnerFromFrontEnd.getPlayerName().equals(playerTwo.getPlayerName()))
                return true;
        }
        return false;
    }

    private MancalaGame simulateGamePlay (MancalaGame gameFromStore) {
        int selectedContainerIndex = gameFromStore.getSelectedStoneContainerIndex();
        int stoneCount = gameFromStore.getStoneContainer(selectedContainerIndex).getAllStonesAndEmptyContainer();

        //start adding stones to the first container after the one selected
        int currentContainerIndex = (selectedContainerIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        ActivePlayer activePlayer = gameFromStore.getActivePlayer();

        //distribute stones to containers based on game rules
        while (stoneCount > 0) {
            //cannot place stones on another player's house
            if (activePlayer == ActivePlayer.PLAYER_ONE && currentContainerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX
                    || activePlayer == ActivePlayer.PLAYER_TWO && currentContainerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                currentContainerIndex = (currentContainerIndex++) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
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
                        int houseIndex = (activePlayer == ActivePlayer.PLAYER_ONE)?
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
            currentContainerIndex = (currentContainerIndex++) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }

        //change player if you didn't place last stone in your own house, assisted by logic above
        if (currentContainerIndex != MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                && currentContainerIndex != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            gameFromStore.setActivePlayer(changePlayer(activePlayer));
        }
        return gameFromStore;
    }

    private ActivePlayer changePlayer (ActivePlayer currentPlayer) {
        if (currentPlayer == ActivePlayer.PLAYER_ONE)
            return ActivePlayer.PLAYER_TWO;
        return ActivePlayer.PLAYER_ONE;
    }
}
