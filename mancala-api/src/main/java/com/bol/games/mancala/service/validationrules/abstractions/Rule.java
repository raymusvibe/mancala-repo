package com.bol.games.mancala.service.validationrules.abstractions;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import org.springframework.data.mongodb.core.MongoTemplate;

public abstract class Rule {

    protected Rule successor;
    public void setSuccessor(Rule successor) {
        this.successor = successor;
    }
    public abstract void processRequest(MancalaGame gameFromFrontEnd,
                                        MancalaGame gameFromStore,
                                        MongoTemplate mancalaGamesMongoTemplate) throws ValidationException;
}
