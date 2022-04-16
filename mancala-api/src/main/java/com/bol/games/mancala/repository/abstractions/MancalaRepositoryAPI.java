package com.bol.games.mancala.repository.abstractions;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.model.MancalaGame;
import org.springframework.stereotype.Repository;

@Repository
public interface MancalaRepositoryAPI {
    MancalaGame findNewGame(String gameId);
    MancalaGame findGame(String gameId);
    MancalaGame saveGame(MancalaGame game);
    void insertGame(MancalaGame game);
    void insertEvent (GamePlay gamePlay);
}
