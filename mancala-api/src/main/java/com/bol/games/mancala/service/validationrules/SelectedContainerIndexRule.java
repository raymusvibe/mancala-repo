package com.bol.games.mancala.service.validationrules;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.model.StoneContainer;
import com.bol.games.mancala.service.validationrules.abstractions.Rule;
import org.springframework.data.mongodb.core.MongoTemplate;

public class SelectedContainerIndexRule extends Rule {
    @Override
    public void processRequest(MancalaGame gameFromFrontEnd,
                               MancalaGame gameFromStore,
                               MongoTemplate mancalaGamesMongoTemplate) throws ValidationException {
        Integer containerIndex = gameFromFrontEnd.getSelectedStoneContainerIndex();
        StoneContainer selectedStoneContainer = gameFromStore.getStoneContainer(containerIndex);
        //no action required for selecting an empty container or house containers
        if (selectedStoneContainer.isEmpty()
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
        successor.processRequest(gameFromFrontEnd, gameFromStore, mancalaGamesMongoTemplate);
    }
}
