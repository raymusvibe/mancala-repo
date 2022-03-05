package com.bol.games.mancala.service;

import com.bol.games.mancala.exception.*;
import com.bol.games.mancala.model.*;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import com.bol.games.mancala.service.validation.*;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service used to validate gameplay from the frontend and enforce game rules.
 */
@Service
@AllArgsConstructor
public class MancalaGamePlayValidationService implements MancalaGamePlayValidationAPI {

    @Autowired
    private MongoTemplate mancalaGamesMongoTemplate;
    @Autowired
    private MongoTemplate mancalaEventsMongoTemplate;

    /**
     * Game play validation method used to validate game play from the frontend.
     * The chain of responsibility pattern is used to enforce the various rules.
     * @param gameFromFrontEnd the mancala game object send from the frontend after a player's move.
     * @return the validated game instance, modified according to game rules.
     */
    @Override
    public final MancalaGame validate (MancalaGame gameFromFrontEnd) throws ValidationException {
        //event log
        mancalaEventsMongoTemplate.insert(gameFromFrontEnd);

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

        gameExistsInStoreRule.processRequest(gameFromFrontEnd, Optional.empty(), mancalaGamesMongoTemplate);

        //If we got this far, correct game state is in DB
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameFromFrontEnd.getGameId()));
        return mancalaGamesMongoTemplate.findOne(query, MancalaGame.class);
    }
}
