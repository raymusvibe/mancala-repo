package com.bol.games.mancala.utils;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

public class DummyRule extends Rule {
    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               Optional<MancalaGame> gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {}
}
