package com.bol.games.mancala.model;

import com.bol.games.mancala.constants.MancalaConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
public class MancalaGame implements Serializable {

    @Id
    private String gameId;

    private Player playerOne;
    private Player playerTwo;
    private GameStatus gamePlayStatus;
    private List<StoneContainer> mancalaBoard;
    private Player winner;
    private ActivePlayer activePlayer;
    private Integer selectedStoneContainerIndex;

    public MancalaGame (Player playerOne) {
        Integer totalNumberOfContainers = (MancalaConstants.CONTAINERS_PER_PLAYER + 1) * 2;
        ArrayList <StoneContainer> stoneContainers = new ArrayList<>();
        for (int i = 0; i < totalNumberOfContainers; i++) {
            if (i != MancalaConstants.PLAYER_ONE_HOUSE_INDEX &&
                    i != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                stoneContainers.add(new StoneContainer(i, MancalaConstants.STONES_PER_CONTAINER));
            } else {
                stoneContainers.add(new HouseStoneContainer(i));
            }
        }
        this.mancalaBoard = stoneContainers;
        this.playerOne = playerOne;
        this.activePlayer = ActivePlayer.PLAYER_ONE;
        this.gameId = UUID.randomUUID().toString();
        this.gamePlayStatus = GameStatus.NEW;
    }

    public StoneContainer getStoneContainer(Integer stoneContainerIndex) {
        return this.mancalaBoard.get(stoneContainerIndex);
    }
}

