package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * Rule used to validate the winner of a game and to determine a winner in case the front-end missed finding a winner.
 */
public class GameWinnerRule extends GameRule {

    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                                     MancalaGame gameFromStore,
                                     MongoTemplate mongoTemplate) throws ValidationException {
        if (gameFromFrontEnd.getWinner() != null && validateWinnerFromFrontEnd(gameFromFrontEnd)) {
            assert gameFromStore != null;
            gameFromStore.setWinner(gameFromFrontEnd.getWinner());
            gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
            gameFromStore.setSelectedStoneContainerIndex(null);
            gameFromStore.setMancalaBoard(gameFromFrontEnd.getMancalaBoard());
            mongoTemplate.save(gameFromStore);
        } else {
            //if frontend missed finding a winner, verify
            Optional<MancalaGame> finishedGameOption = gameFromFrontEnd.finishGame();
            if (finishedGameOption.isPresent()) {
                MancalaGame finishedGame = finishedGameOption.get();
                assert gameFromStore != null;
                gameFromStore.setWinner(finishedGame.getWinner());
                gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
                gameFromStore.setSelectedStoneContainerIndex(null);
                gameFromStore.setMancalaBoard(finishedGame.getMancalaBoard());
                mongoTemplate.save(gameFromStore);
            } else {
                successor.processRequest(gameFromFrontEnd, gameFromStore, mongoTemplate);
            }
        }
    }

    /**
     * This method validates the input from the frontend based on the number of stones.
     * @param gameFromFrontEnd game from frontend
     * @return boolean true if the frontend did not make an error
     */
    private boolean validateWinnerFromFrontEnd(MancalaGame gameFromFrontEnd) throws ValidationException {
        int playerOneStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
        int playerTwoStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
        switch (gameFromFrontEnd.getWinner()) {
            case PLAYER_ONE:
                if (playerOneStones > playerTwoStones) {
                    return true;
                }
                break;
            case PLAYER_TWO:
                if (playerOneStones < playerTwoStones) {
                    return true;
                }
                break;
            case DRAW:
                if (playerOneStones == playerTwoStones) {
                    return true;
                }
                break;
            default:
                throw new ValidationException ("Unexpected game winner value: " + gameFromFrontEnd.getWinner());
        }
        throw new ValidationException("Invalid game state (Game winner status incorrect)");
    }
}
