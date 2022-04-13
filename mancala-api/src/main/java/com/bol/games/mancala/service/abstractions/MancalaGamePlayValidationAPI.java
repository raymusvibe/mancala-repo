package com.bol.games.mancala.service.abstractions;

import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;

public interface MancalaGamePlayValidationAPI {
    MancalaGame validate (MancalaGame game);
}
