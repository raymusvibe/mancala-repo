package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.model.GameWinner;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to validate the winner of a game and to determine a winner in case the front-end missed finding a winner.
 * At this point in the validation chain the Mancala board from the frontend would have been validated and is identical
 * to store board after the stone sowing rule prior to this rule. Only the game winner and game status need to be updated
 * in the store object if there is a winner.
 */
public class GameWinnerRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                                     MancalaGame gameFromStore,
                                     MancalaRepository mancalaRepository) throws ValidationException {
        if (gameFromFrontEnd.getWinner() != null && validateWinnerFromFrontEnd(gameFromFrontEnd)) {
            gameFromStore.setWinner(gameFromFrontEnd.getWinner());
            gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
            mancalaRepository.saveGame(gameFromStore);
        } else if (isGameFinished(gameFromStore)) {
            //If frontend missed finding a winner, it's prudent to check
            int playerOneFinalStoneCount = gameFromStore.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
            int playerTwoFinalStoneCount = gameFromStore.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
            if (playerOneFinalStoneCount > playerTwoFinalStoneCount) {
                gameFromStore.setWinner(GameWinner.PLAYER_ONE);
            } else if (playerOneFinalStoneCount < playerTwoFinalStoneCount) {
                gameFromStore.setWinner(GameWinner.PLAYER_TWO);
            } else {
                gameFromStore.setWinner(GameWinner.DRAW);
            }
            gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
            mancalaRepository.saveGame(gameFromStore);
        }
    }

    /**
     * This method validates the input from the frontend based on the number of stones.
     * @param gameFromFrontEnd game from frontend
     * @return boolean true if the frontend did not make an error
     */
    private boolean validateWinnerFromFrontEnd(MancalaGame gameFromFrontEnd) throws ValidationException {
        movePlayerStonesToHouse(gameFromFrontEnd, Player.PLAYER_ONE);
        movePlayerStonesToHouse(gameFromFrontEnd, Player.PLAYER_TWO);
        int playerOneStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
        int playerTwoStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
        boolean statusIsCorrect = false;
        switch (gameFromFrontEnd.getWinner()) {
            case PLAYER_ONE:
                if (playerOneStones > playerTwoStones) {
                    statusIsCorrect = true;
                }
                break;
            case PLAYER_TWO:
                if (playerOneStones < playerTwoStones) {
                    statusIsCorrect = true;
                }
                break;
            case DRAW:
                if (playerOneStones == playerTwoStones) {
                    statusIsCorrect = true;
                }
                break;
            default:
                throw new ValidationException ("Unexpected game winner value: " + gameFromFrontEnd.getWinner());
        }
        if (statusIsCorrect){
            return true;
        } else {
            throw new ValidationException("Invalid game state (Game winner status incorrect)");
        }
    }

    /*
    * TODO
    * */
    private boolean isGameFinished (MancalaGame gameFromStore) {
        boolean isFinished = false;
        int playerOneStoneCount = MancalaConstants.EMPTY_STONE_COUNT;
        for (int i = 0; i < MancalaConstants.PLAYER_ONE_HOUSE_INDEX; i++) {
            playerOneStoneCount += gameFromStore.getStoneContainer(i).getStones();
        }
        if (playerOneStoneCount == MancalaConstants.EMPTY_STONE_COUNT) {
            isFinished = true;
            movePlayerStonesToHouse(gameFromStore, Player.PLAYER_TWO);
        } else {
            int playerTwoStoneCount = MancalaConstants.EMPTY_STONE_COUNT;
            for (int i = MancalaConstants.PLAYER_ONE_HOUSE_INDEX + 1; i < MancalaConstants.PLAYER_TWO_HOUSE_INDEX; i++) {
                playerTwoStoneCount += gameFromStore.getStoneContainer(i).getStones();
            }
            if (playerTwoStoneCount == MancalaConstants.EMPTY_STONE_COUNT) {
                isFinished = true;
                movePlayerStonesToHouse(gameFromStore, Player.PLAYER_ONE);
            }
        }
        return isFinished;
    }

    /*
     * TODO
     * */
    private void movePlayerStonesToHouse (MancalaGame game, Player player) {
        if (player == Player.PLAYER_ONE) {
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
