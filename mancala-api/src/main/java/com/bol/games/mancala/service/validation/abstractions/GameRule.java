package com.bol.games.mancala.service.validation.abstractions;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;

public abstract class GameRule {
    protected GameRule successor;
    public final void setSuccessor(GameRule successor) {this.successor = successor;}
    public abstract void processRequest(MancalaGame gameFromFrontEnd,
                                        MancalaGame gameFromStore,
                                        MancalaRepository mancalaRepository) throws ValidationException;
}
