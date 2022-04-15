package com.bol.games.mancala.service;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.*;
import com.bol.games.mancala.model.*;
import com.bol.games.mancala.repository.abstractions.MancalaRepositoryAPI;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import com.bol.games.mancala.service.validation.*;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service used to validate gameplay from the frontend and enforce game rules.
 */
@Service
@AllArgsConstructor
@Slf4j
public class MancalaGamePlayValidationService implements MancalaGamePlayValidationAPI {

    @Autowired
    private MancalaRepositoryAPI mancalaRepository;

    /**
     * Game play validation method used to validate game play from the frontend.
     * When validation fails, service returns correct state to frontend unless
     * the game ID provided was not found and a NotFoundException is thrown.
     * The chain of responsibility pattern is used to enforce the various rules.
     * @param gameFromFrontEnd the mancala game object send from the frontend.
     * @return the validated game instance, modified according to game rules.
     */
    @Override
    public final MancalaGame validate (MancalaGame gameFromFrontEnd) throws Exception{
        mancalaRepository.insertEvent(gameFromFrontEnd);

        GameRule gameExistsInStoreRule = new GameExistsInStoreRule();
        GameRule gameRestartRequestRule = new GameRestartRequestRule();
        GameRule stoneCountRule = new StoneCountRule();
        GameRule gameWinnerRule = new GameWinnerRule();
        GameRule selectedContainerIndexRule = new SelectedContainerIndexRule();
        GameRule stoneSowingRule = new StoneSowingRule();
        gameExistsInStoreRule.setSuccessor(gameRestartRequestRule);
        gameRestartRequestRule.setSuccessor(stoneCountRule);
        stoneCountRule.setSuccessor(selectedContainerIndexRule);
        selectedContainerIndexRule.setSuccessor(stoneSowingRule);
        stoneSowingRule.setSuccessor(gameWinnerRule);
        try {
            gameExistsInStoreRule.processRequest(gameFromFrontEnd, null, (MancalaRepository) mancalaRepository);
        } catch (ValidationException e) {
            log.error("Error validating gameplay", e);
        }
        //Validated or correct game state will be in the cache and the DB
        return mancalaRepository.findGame(gameFromFrontEnd.getGameId());
    }
}
