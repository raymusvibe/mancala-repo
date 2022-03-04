package com.bol.games.mancala.service.validation;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validation.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * Rule used to validate the selected container index.
 */

public class SelectedContainerIndexRule extends Rule {
    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               Optional<MancalaGame> gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        Integer containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        StoneContainer selectedStoneContainer = gameFromStore.get().getStoneContainer(containerIndex);
        //no action required for selecting an empty container or house containers
        if (selectedStoneContainer.isEmpty()
                || containerIndex == MancalaConstants.PlayerOneHouseIndex
                || containerIndex == MancalaConstants.PlayerTwoHouseIndex) {
            return;
        }
        //player cannot start from opponents side
        if (gameFromFrontEnd.getActivePlayer() == Player.PlayerOne && containerIndex > MancalaConstants.PlayerOneHouseIndex
                || gameFromFrontEnd.getActivePlayer() == Player.PlayerTwo && containerIndex < MancalaConstants.PlayerOneHouseIndex
                || gameFromFrontEnd.getActivePlayer() == Player.PlayerTwo && containerIndex > MancalaConstants.PlayerTwoHouseIndex) {
            throw new ValidationException("Invalid game state detected (Container selection index)");
        }
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
    }
}
