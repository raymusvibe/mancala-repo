package com.bol.games.mancala.model;

import com.bol.games.mancala.constants.MancalaConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "mancala")
@Setter
@Getter
public class MancalaGame implements Serializable {
    @Id
    @JsonIgnore
    private String id;

    private String gameId;
    private GameStatus gamePlayStatus;
    private Player activePlayer;
    private GameWinner winner;
    private Integer selectedStoneContainerIndex;
    private List<StoneContainer> mancalaBoard;

    public MancalaGame () {
        this.gameId = UUID.randomUUID().toString();
        activePlayer = Player.PLAYER_ONE;
        gamePlayStatus = GameStatus.NEW;
        selectedStoneContainerIndex = MancalaConstants.PLAYER_ONE_HOUSE_INDEX;
    }

    @JsonIgnore
    public final void initialiseBoardToStartNewGame() {
        mancalaBoard = new ArrayList<>();
        int numberOfContainers = (MancalaConstants.CONTAINERS_PER_PLAYER + 1) * 2;
        for (int i = 0; i < numberOfContainers; i++) {
            if (i == MancalaConstants.PLAYER_ONE_HOUSE_INDEX ||
                    i == MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                mancalaBoard.add(new HouseStoneContainer(i));
            } else {
                mancalaBoard.add(new StoneContainer(i, MancalaConstants.STONES_PER_PLAYER));
            }
        }
    }

    @JsonIgnore
    public final StoneContainer getStoneContainer(Integer stoneContainerIndex) {
        return mancalaBoard.get(stoneContainerIndex);
    }
}

