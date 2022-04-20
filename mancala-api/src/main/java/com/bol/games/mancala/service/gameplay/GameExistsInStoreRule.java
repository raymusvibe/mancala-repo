package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;

/**
 * Rule used to verify the incoming game id with the database.
 * The rule will not pass on the chain of responsibility if the game
 * status is DISRUPTED. The DISRUPTED game status is only a transitive status,
 * used when the client has experienced a problem and needs to sync with the service.
 */
public class GameExistsInStoreRule extends GameRule {
    @Override
    public final void executeRule(GamePlay gamePlay,
                                  MancalaGame game,
                                  MancalaRepository mancalaRepository) throws ValidationException {
        MancalaGame gameFromRepo = mancalaRepository.findGame(gamePlay.getGameId());
        if (gameFromRepo == null) {
            throw new ValidationException("Invalid game Id provided: " + gamePlay.getGameId());
        }
        if (gamePlay.getGamePlayStatus() != GameStatus.DISRUPTED) {
            successor.executeRule(gamePlay, gameFromRepo, mancalaRepository);
        }
    }
}
