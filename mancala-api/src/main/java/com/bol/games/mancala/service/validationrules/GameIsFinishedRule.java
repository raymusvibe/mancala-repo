package com.bol.games.mancala.service.validationrules;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

public class GameIsFinishedRule extends Rule {

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

    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        //frontend determined a winner
        if (gameFromFrontEnd.getWinner() != null) {
            if (validateWinnerFromFrontEnd(gameFromFrontEnd)) {
                gameFromStore.setWinner(gameFromFrontEnd.getWinner());
                gameFromStore.setGamePlayStatus(GameStatus.Finished);
                gameFromStore.setSelectedStoneContainerIndex(null);
                gameFromStore.setMancalaBoard(gameFromFrontEnd.getMancalaBoard());
                mancalaGamesMongoTemplate.save(gameFromStore);
                return;
            }
        }
        //if frontend missed finding a winner, verify
        Optional<MancalaGame> finishedGameOption = gameFromFrontEnd.isGameFinished();
        if (!finishedGameOption.isEmpty()) {
            MancalaGame finishedGame = finishedGameOption.get();
            gameFromStore.setWinner(finishedGame.getWinner());
            gameFromStore.setGamePlayStatus(GameStatus.Finished);
            gameFromStore.setSelectedStoneContainerIndex(null);
            gameFromStore.setMancalaBoard(finishedGame.getMancalaBoard());
            mancalaGamesMongoTemplate.save(gameFromStore);
            return;
        }
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
    }
}
