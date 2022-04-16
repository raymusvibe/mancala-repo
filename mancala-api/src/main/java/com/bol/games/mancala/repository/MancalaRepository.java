package com.bol.games.mancala.repository;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.repository.abstractions.MancalaRepositoryAPI;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * Mongo repository which abstracts the Mongo template and facilitates Redis caching.
 * */
@Component
@AllArgsConstructor
public class MancalaRepository implements MancalaRepositoryAPI {

    @Autowired
    private MongoTemplate mancalaGamesMongoTemplate;
    @Autowired
    private MongoTemplate mancalaEventsMongoTemplate;

    @Override
    public MancalaGame findNewGame(String gameId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameId).and("gamePlayStatus").is(GameStatus.NEW));
        return mancalaGamesMongoTemplate.findOne(query, MancalaGame.class);
    }

    @Override
    @Cacheable(value = "mancalaCache", key = "#gameId", unless = "#result == null")
    public MancalaGame findGame(String gameId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameId));
        return mancalaGamesMongoTemplate.findOne(query, MancalaGame.class);
    }

    @Override
    @CachePut(value="mancalaCache", key="#game.gameId")
    public MancalaGame saveGame(MancalaGame game) {
        return mancalaGamesMongoTemplate.save(game);
    }

    @Override
    public void insertGame(MancalaGame game) {
        mancalaGamesMongoTemplate.insert(game);
    }

    @Override
    public void insertEvent(GamePlay game) {
        mancalaEventsMongoTemplate.insert(game);
    }
}
