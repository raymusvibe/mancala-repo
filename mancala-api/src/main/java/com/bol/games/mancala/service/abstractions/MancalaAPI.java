package com.bol.games.mancala.service.abstractions;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.exception.NotFoundException;

public interface MancalaAPI<T> {
    public <T> T createGame() throws ValidationException;
    public <T> T connectToGame(String gameId) throws NotFoundException;
}
