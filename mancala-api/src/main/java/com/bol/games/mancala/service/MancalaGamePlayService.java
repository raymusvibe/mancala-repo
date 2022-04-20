package com.bol.games.mancala.service;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.*;
import com.bol.games.mancala.repository.abstractions.MancalaRepositoryAPI;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayAPI;
import com.bol.games.mancala.service.gameplay.*;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service used to enforce game rules.
 */
@Service
@AllArgsConstructor
@Slf4j
public class MancalaGamePlayService implements MancalaGamePlayAPI {

    @Autowired
    private MancalaRepositoryAPI mancalaRepository;

    /**
     * This method executes game rules using the game play object send by the frontend.
     * The frontend could, for instance, be asking though the gameplay object for the
     * updated Mancala board after selecting a pot for sowing or asking for the correct
     * state of the game after a service disruption, among other scenarios handled by
     * the game rules.
     * The chain of responsibility pattern is used to enforce the various game rules.
     * @param gamePlay the game play object send from the frontend.
     * @return MancalaGame, the validated and updated game instance.
     */
    @Override
    public final MancalaGame executeGameRules(GamePlay gamePlay) throws ValidationException {
        mancalaRepository.insertEvent(gamePlay);

        GameRule gameExistsInStoreRule = new GameExistsInStoreRule();
        GameRule gameRestartRequestRule = new GameRestartRequestRule();
        GameRule selectedContainerIndexRule = new SelectedContainerIndexRule();
        GameRule stoneSowingRule = new StoneSowingRule();
        GameRule gameWinnerRule = new GameWinnerRule();
        gameExistsInStoreRule.setSuccessor(gameRestartRequestRule);
        gameRestartRequestRule.setSuccessor(selectedContainerIndexRule);
        selectedContainerIndexRule.setSuccessor(stoneSowingRule);
        stoneSowingRule.setSuccessor(gameWinnerRule);

        gameExistsInStoreRule.executeRule(gamePlay, null, (MancalaRepository) mancalaRepository);

        return mancalaRepository.findGame(gamePlay.getGameId());
    }
}
