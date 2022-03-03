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
        int allocatedNumberOfStonesPerPlayer = MancalaConstants.CONTAINERS_PER_PLAYER * MancalaConstants.STONES_PER_CONTAINER;
        int playerOneStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
        int playerTwoStones = gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
        if (playerOneStones + playerTwoStones != allocatedNumberOfStonesPerPlayer * 2)
            throw new ValidationException("Error validating stone count for game winner");
        switch (gameFromFrontEnd.getWinner()) {
            case PLAYER_ONE:
                if (playerOneStones > playerTwoStones)
                    return true;
                break;
            case PLAYER_TWO:
                if (playerOneStones < playerTwoStones)
                    return true;
                break;
            case DRAW:
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
                gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
                gameFromStore.setSelectedStoneContainerIndex(null);
                gameFromStore.setMancalaBoard(gameFromFrontEnd.getMancalaBoard());
                mancalaGamesMongoTemplate.save(gameFromStore);
                return;
            }
        }
        //if frontend missed finding a winner, verify after checking stone count
        Optional<MancalaGame> finishedGameOption = gameFromFrontEnd.isGameFinished();
        if (!finishedGameOption.isEmpty()) {
            MancalaGame finishedGame = finishedGameOption.get();
            gameFromStore.setWinner(finishedGame.getWinner());
            gameFromStore.setGamePlayStatus(GameStatus.FINISHED);
            gameFromStore.setSelectedStoneContainerIndex(null);
            gameFromStore.setMancalaBoard(finishedGame.getMancalaBoard());
            mancalaGamesMongoTemplate.save(gameFromStore);
            return;
        }
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
    }
}
