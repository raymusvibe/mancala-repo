package com.bol.games.mancala.service.abstractions;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;

public interface MancalaGamePlayAPI {
    MancalaGame executeGameRules(GamePlay gamePlay) throws ValidationException;
}
