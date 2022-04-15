package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to validate the stone count.
 */
public class StoneCountRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MancalaRepository mancalaRepository) throws Exception {
        int stoneCount = gameFromFrontEnd.getMancalaBoard()
                        .stream()
                        .map(StoneContainer::getStones)
                        .reduce(0, Integer::sum);
        int expectedStoneCount = MancalaConstants.CONTAINERS_PER_PLAYER * MancalaConstants.STONES_PER_PLAYER * 2;
        if (stoneCount != expectedStoneCount) {
            throw new ValidationException("Error validating stone count");
        }
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaRepository);
    }
}
