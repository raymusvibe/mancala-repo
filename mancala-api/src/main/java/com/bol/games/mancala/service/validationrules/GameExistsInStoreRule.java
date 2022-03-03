package com.bol.games.mancala.service.validationrules;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class GameExistsInStoreRule extends Rule {

    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameFromFrontEnd.getGameId()));
        MancalaGame gameFromRepo = mancalaGamesMongoTemplate.findOne(query, MancalaGame.class);
        if (gameFromRepo == null) {
            throw new ValidationException("Invalid game Id provided: " + gameFromFrontEnd.getGameId());
        }
        successor.processRequest(gameFromFrontEnd, gameFromRepo, mancalaGamesMongoTemplate);
    }
}
