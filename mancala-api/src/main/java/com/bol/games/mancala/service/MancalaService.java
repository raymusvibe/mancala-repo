package com.bol.games.mancala.service;

import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MancalaService implements MancalaAPI {

    private MongoTemplate mancalaGamesMongoTemplate;
    private MongoTemplate mancalaEventsMongoTemplate;

    @Override
    public MancalaGame createGame() {
        MancalaGame mancala = new MancalaGame();
        mancalaGamesMongoTemplate.insert(mancala);
        mancalaEventsMongoTemplate.insert(mancala);
        return mancala;
    }

    @Override
    public MancalaGame connectToGame(String gameId) throws NotFoundException {
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameId).and("gamePlayStatus").is(GameStatus.NEW));
        MancalaGame game = mancalaGamesMongoTemplate.findOne(query, MancalaGame.class);
        if (game == null) {
            throw new NotFoundException("Invalid GameId or this game is already in progress");
        }
        game.setGamePlayStatus(GameStatus.IN_PROGRESS);
        mancalaGamesMongoTemplate.save(game);
        return game;
    }
}
