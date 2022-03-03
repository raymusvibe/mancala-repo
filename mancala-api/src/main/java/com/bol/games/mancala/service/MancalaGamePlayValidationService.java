package com.bol.games.mancala.service;

import com.bol.games.mancala.exception.*;
import com.bol.games.mancala.model.*;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import com.bol.games.mancala.service.validationrules.*;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MancalaGamePlayValidationService implements MancalaGamePlayValidationAPI {

    private MongoTemplate mancalaGamesMongoTemplate;
    private MongoTemplate mancalaEventsMongoTemplate;

    @Override
    public MancalaGame validate(MancalaGame gameFromFrontEnd) throws ValidationException {
        //event logging
        mancalaEventsMongoTemplate.insert(gameFromFrontEnd);

        //validation rules
        Rule gameExistsInStoreRule = new GameExistsInStoreRule();
        Rule newGameRequestRule = new NewGameRequestRule();
        Rule stoneCountRule = new StoneCountRule();
        Rule gameIsFinishedRule = new GameIsFinishedRule();
        Rule selectedContainerIndexRule = new SelectedContainerIndexRule();
        Rule stoneSowingRule = new StoneSowingRule();
        //chain of responsibility
        gameExistsInStoreRule.setSuccessor(newGameRequestRule);
        newGameRequestRule.setSuccessor(stoneCountRule);
        stoneCountRule.setSuccessor(gameIsFinishedRule);
        gameIsFinishedRule.setSuccessor(selectedContainerIndexRule);
        selectedContainerIndexRule.setSuccessor(stoneSowingRule);

        gameExistsInStoreRule.processRequest(gameFromFrontEnd, null, mancalaGamesMongoTemplate);
        //If we got this far, correct game state is in DB
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameFromFrontEnd.getGameId()));
        return mancalaGamesMongoTemplate.findOne(query, MancalaGame.class);
    }
}
