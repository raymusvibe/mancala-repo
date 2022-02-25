package com.bol.games.mancala.service.abstractions;

import com.bol.games.mancala.exception.IllegalRequestException;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.exception.InvalidGameException;
import com.bol.games.mancala.exception.NotFoundException;

public interface MancalaAPI<T> {
    public <T> T createGame(Player playerOne) throws ValidationException;
    public <T> T connectToGame(Player playerTwo, String gameId) throws InvalidGameException, IllegalRequestException, ValidationException;
    public <T> T connectToRandomGame(Player playerTwo) throws NotFoundException, ValidationException;
}
