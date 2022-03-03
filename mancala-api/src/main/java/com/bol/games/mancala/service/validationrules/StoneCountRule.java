package com.bol.games.mancala.service.validationrules;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

public class StoneCountRule extends Rule {

    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        Integer sum = gameFromFrontEnd.getMancalaBoard()
                .stream()
                .map(x -> x.getStones())
                .reduce(0, Integer::sum);
        int totalNumberOfStones = MancalaConstants.ContainersPerPlayer * MancalaConstants.StonesPerPlayer * 2;
        if (totalNumberOfStones != sum) {
            throw new ValidationException("Error validating stone count");
        }

        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
    }
}
