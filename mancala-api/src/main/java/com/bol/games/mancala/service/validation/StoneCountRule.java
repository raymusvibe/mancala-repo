package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Rule used to validate the stone count.
 */
public class StoneCountRule extends GameRule {

    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MongoTemplate mongoTemplate) throws ValidationException {
        Integer sum = gameFromFrontEnd.getMancalaBoard()
                .stream()
                .map(StoneContainer::getStones)
                .reduce(0, Integer::sum);
        int stoneCount = MancalaConstants.CONTAINERS_PER_PLAYER * MancalaConstants.STONES_PER_PLAYER * 2;
        if (stoneCount != sum) {
            throw new ValidationException("Error validating stone count");
        }

        successor.processRequest(gameFromFrontEnd, gameFromStore, mongoTemplate);
    }
}
