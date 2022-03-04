package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * Rule used to validate the finished state of a game and to determine this in case the front-end missed this.
 */
public class GameIsFinishedRule extends Rule {

    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               Optional<MancalaGame> gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        //frontend determined a winner
        if (gameFromFrontEnd.getWinner() != null) {
            if (validateWinnerFromFrontEnd(gameFromFrontEnd)) {
                gameFromStore.get().setWinner(gameFromFrontEnd.getWinner());
                gameFromStore.get().setGamePlayStatus(GameStatus.Finished);
                gameFromStore.get().setSelectedStoneContainerIndex(null);
                gameFromStore.get().setMancalaBoard(gameFromFrontEnd.getMancalaBoard());
                mancalaGamesMongoTemplate.save(gameFromStore.get());
                return;
            }
        }
        //if frontend missed finding a winner, verify
        Optional<MancalaGame> finishedGameOption = gameFromFrontEnd.isGameFinished();
        if (!finishedGameOption.isEmpty()) {
            MancalaGame finishedGame = finishedGameOption.get();
            gameFromStore.get().setWinner(finishedGame.getWinner());
            gameFromStore.get().setGamePlayStatus(GameStatus.Finished);
            gameFromStore.get().setSelectedStoneContainerIndex(null);
            gameFromStore.get().setMancalaBoard(finishedGame.getMancalaBoard());
            mancalaGamesMongoTemplate.save(gameFromStore.get());
            return;
        }
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
    }

    private boolean validateWinnerFromFrontEnd(MancalaGame gameFromFrontEnd) throws ValidationException {
        int allocatedNumberOfStonesPerPlayer = MancalaConstants.ContainersPerPlayer * MancalaConstants.StonesPerPlayer;
        int playerOneStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PlayerOneHouseIndex).getStones();
        int playerTwoStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PlayerTwoHouseIndex).getStones();
        if (playerOneStones + playerTwoStones != allocatedNumberOfStonesPerPlayer * 2)
            throw new ValidationException("Error validating stone count for game winner");
        switch (gameFromFrontEnd.getWinner()) {
            case PlayerOne:
                if (playerOneStones > playerTwoStones)
                    return true;
                break;
            case PlayerTwo:
                if (playerOneStones < playerTwoStones)
                    return true;
                break;
            case Draw:
                if (playerOneStones == playerTwoStones)
                    return true;
        }
        throw new ValidationException("Invalid game state detected (Game winner status incorrect)");
    }
}
