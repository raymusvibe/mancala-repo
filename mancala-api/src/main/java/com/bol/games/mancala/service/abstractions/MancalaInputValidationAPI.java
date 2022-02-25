package com.bol.games.mancala.service.abstractions;

import com.bol.games.mancala.exception.ValidationException;

public interface MancalaInputValidationAPI {
    public void validatePlayerName(String input) throws ValidationException;
}
