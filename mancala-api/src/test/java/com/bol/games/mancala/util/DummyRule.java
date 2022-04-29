package com.bol.games.mancala.util;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;

public class DummyRule extends GameRule {
    @Override
    public final void executeRule(GamePlay gamePlay,
                                  MancalaGame gameFromStore,
                                  MancalaRepository mancalaRepository){
        //do nothing
    }
}
