package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to validate the incoming game id with the database.
 */
public class GameExistsInStoreRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                                     MancalaGame gameFromStore,
                                     MancalaRepository mancalaRepository) throws Exception {
        MancalaGame gameFromRepo = mancalaRepository.findGame(gameFromFrontEnd.getGameId());
        if (gameFromRepo == null) {
            throw new NotFoundException ("Invalid game Id provided: " + gameFromFrontEnd.getGameId());
        }
        //if game status is DISRUPTED, client is asking for correct game state after re-connection or error, simply return
        if (gameFromFrontEnd.getGamePlayStatus() != GameStatus.DISRUPTED) {
            successor.processRequest(gameFromFrontEnd, gameFromRepo, mancalaRepository);
        }
    }
}
