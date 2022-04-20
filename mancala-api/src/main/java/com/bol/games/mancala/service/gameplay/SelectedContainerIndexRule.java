package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;

/**
 * Rule used to validate the selected container/pot index.
 */
public class SelectedContainerIndexRule extends GameRule {
    @Override
    public final void executeRule(GamePlay gamePlay,
                                  MancalaGame game,
                                  MancalaRepository mancalaRepository) throws ValidationException {

        if (game.getGamePlayStatus() != GameStatus.IN_PROGRESS) {
            throw new ValidationException("Invalid game status: " + game.getGamePlayStatus());
        }

        int containerIndex = gamePlay.getSelectedStoneContainerIndex();
        StoneContainer targetContainer = game.getStoneContainer(containerIndex);
        //no action required for selecting an empty container or house containers
        if (targetContainer.isEmpty()
                || containerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || containerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            return;
        }
        //player cannot start from opponents side
        if (game.getActivePlayer() == Player.PLAYER_ONE && containerIndex > MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || game.getActivePlayer() == Player.PLAYER_TWO && containerIndex < MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || game.getActivePlayer() == Player.PLAYER_TWO && containerIndex > MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            throw new ValidationException("Invalid game state (Container selection index)");
        }
        successor.executeRule(gamePlay, game, mancalaRepository);
    }
}
