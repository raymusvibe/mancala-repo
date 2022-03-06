package com.bol.games.mancala.utils;

import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

public class DummyRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                                     MancalaGame gameFromStore,
                                     MongoTemplate mongoTemplate) {
        //do nothing
    }
}
