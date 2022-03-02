package com.bol.games.mancala.data;

import com.bol.games.mancala.model.MancalaGame;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MancalaRepository extends MongoRepository<MancalaGame, String> { }
