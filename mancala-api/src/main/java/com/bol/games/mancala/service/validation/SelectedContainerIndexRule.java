package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validation.abstractions.GameRule;

/**
 * Rule used to validate the selected container index.
 */
public class SelectedContainerIndexRule extends GameRule {
    @Override
    public final void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MancalaRepository mancalaRepository) throws ValidationException {
        int containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        assert gameFromStore != null;
        StoneContainer targetContainer = gameFromStore.getStoneContainer(containerIndex);
        //no action required for selecting an empty container or house containers
        if (targetContainer.isEmpty()
                || containerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || containerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            return;
        }
        //player cannot start from opponents side
        if (gameFromFrontEnd.getActivePlayer() == Player.PLAYER_ONE && containerIndex > MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || gameFromFrontEnd.getActivePlayer() == Player.PLAYER_TWO && containerIndex < MancalaConstants.PLAYER_ONE_HOUSE_INDEX
                || gameFromFrontEnd.getActivePlayer() == Player.PLAYER_TWO && containerIndex > MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
            throw new ValidationException("Invalid game state detected (Container selection index)");
        }
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaRepository);
    }
}
