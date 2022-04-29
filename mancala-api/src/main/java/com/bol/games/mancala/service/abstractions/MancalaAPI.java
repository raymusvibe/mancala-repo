package com.bol.games.mancala.service.abstractions;

import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.model.MancalaGame;

public interface MancalaAPI {
    MancalaGame createGame();
    MancalaGame connectToGame(String gameId) throws NotFoundException;
}
