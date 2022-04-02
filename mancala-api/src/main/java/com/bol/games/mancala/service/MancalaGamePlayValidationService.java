package com.bol.games.mancala.service;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.*;
import com.bol.games.mancala.model.*;
import com.bol.games.mancala.repository.abstractions.MancalaRepositoryAPI;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import com.bol.games.mancala.service.validation.*;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service used to validate gameplay from the frontend and enforce game rules.
 */
@Service
@AllArgsConstructor
public class MancalaGamePlayValidationService implements MancalaGamePlayValidationAPI {

    @Autowired
    private MancalaRepositoryAPI mancalaRepository;

    /**
     * Game play validation method used to validate game play from the frontend.
     * The chain of responsibility pattern is used to enforce the various rules.
     * @param gameFromFrontEnd the mancala game object send from the frontend after a player's move.
     * @return the validated game instance, modified according to game rules.
     */
    @Override
    public final MancalaGame validate (MancalaGame gameFromFrontEnd) throws ValidationException {
        //event log
        mancalaRepository.insertEvent(gameFromFrontEnd);
        //rules and chain of responsibility
        GameRule gameExistsInStoreRule = new GameExistsInStoreRule();
        GameRule newGameRequestRule = new NewGameRequestRule();
        GameRule stoneCountRule = new StoneCountRule();
        GameRule gameWinnerRule = new GameWinnerRule();
        GameRule selectedContainerIndexRule = new SelectedContainerIndexRule();
        GameRule stoneSowingRule = new StoneSowingRule();
        gameExistsInStoreRule.setSuccessor(newGameRequestRule);
        newGameRequestRule.setSuccessor(stoneCountRule);
        stoneCountRule.setSuccessor(gameWinnerRule);
        gameWinnerRule.setSuccessor(selectedContainerIndexRule);
        selectedContainerIndexRule.setSuccessor(stoneSowingRule);
        //rules execution
        gameExistsInStoreRule.processRequest(gameFromFrontEnd, null, (MancalaRepository) mancalaRepository);
        //if we got this far, correct game state is in the DB and the cache
        return mancalaRepository.findGame(gameFromFrontEnd.getGameId());
    }
}
