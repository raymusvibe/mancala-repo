package com.bol.games.mancala.service.gameplay.abstractions;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;

public abstract class GameRule {
    protected GameRule successor;
    public final void setSuccessor(GameRule successor) {this.successor = successor;}
    public abstract void executeRule(GamePlay gamePlay,
                                     MancalaGame gameFromStore,
                                     MancalaRepository mancalaRepository) throws Exception;
}
