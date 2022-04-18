package com.bol.games.mancala.repository;

import com.bol.games.mancala.model.MancalaGame;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.mongodb.repository.MongoRepository;

@TestComponent
public interface MancalaTestRepository extends MongoRepository <MancalaGame, String> {
}
