package com.bol.games.mancala.model;

import com.bol.games.mancala.constants.MancalaConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Document(collection = "mancala")
@Setter
@Getter
@AllArgsConstructor
public class MancalaGame implements Serializable {

    @Id
    private String id;

    private String gameId;
    private GameStatus gamePlayStatus;
    private List<StoneContainer> mancalaBoard;
    private Player activePlayer;
    private GameWinner winner;
    private Integer selectedStoneContainerIndex;

    public MancalaGame () {
        initialiseBoard();
        activePlayer = Player.PlayerOne;
        gameId = UUID.randomUUID().toString();
        gamePlayStatus = GameStatus.New;
    }

    public void initialiseBoard() {
        ArrayList <StoneContainer> stoneContainers = new ArrayList<>();
        Integer totalNumberOfContainers = (MancalaConstants.ContainersPerPlayer + 1) * 2;
        for (int i = 0; i < totalNumberOfContainers; i++) {
            if (i != MancalaConstants.PlayerOneHouseIndex &&
                    i != MancalaConstants.PlayerTwoHouseIndex) {
                stoneContainers.add(new StoneContainer(i, MancalaConstants.StonesPerPlayer));
            } else {
                stoneContainers.add(new HouseStoneContainer(i));
            }
        }
        mancalaBoard = stoneContainers;
    }

    @JsonIgnore
    public StoneContainer getStoneContainer(Integer stoneContainerIndex) {
        return mancalaBoard.get(stoneContainerIndex);
    }

    @JsonIgnore
    public Optional<MancalaGame> isGameFinished () {
        int playerOneStoneCount = 0;
        for (int i = 0; i < MancalaConstants.PlayerOneHouseIndex; i++) {
            playerOneStoneCount += getStoneContainer(i).getStones();
        }
        if (playerOneStoneCount == 0) {
            movePlayerStonesToHouse(Player.PlayerTwo);
        } else {
            int playerTwoStoneCount = 0;
            for (int i = MancalaConstants.PlayerOneHouseIndex + 1; i < MancalaConstants.PlayerTwoHouseIndex; i++) {
                playerTwoStoneCount += getStoneContainer(i).getStones();
            }
            if (playerTwoStoneCount == 0) {
                movePlayerStonesToHouse(Player.PlayerOne);
            } else {
                //game isn't finished
                return Optional.empty();
            }
        }

        //Game has ended if we reach this section
        int playerOneFinalStoneCount = getStoneContainer(MancalaConstants.PlayerOneHouseIndex).getStones();
        int playerTwoFinalStoneCount = getStoneContainer(MancalaConstants.PlayerTwoHouseIndex).getStones();

        if (playerOneFinalStoneCount > playerTwoFinalStoneCount) {
            setWinner(GameWinner.PlayerOne);
        } else if (playerOneFinalStoneCount < playerTwoFinalStoneCount) {
            setWinner(GameWinner.PlayerTwo);
        } else {
            setWinner(GameWinner.Draw);
        }

        setGamePlayStatus(GameStatus.Finished);
        return Optional.of(this);
    }

    @JsonIgnore
    private void movePlayerStonesToHouse (Player player) {
        switch (player) {
            case PlayerOne:
                for (int i = 0; i < MancalaConstants.PlayerOneHouseIndex; i++) {
                    mancalaBoard.get(MancalaConstants.PlayerOneHouseIndex)
                            .addStones(mancalaBoard.get(i).getAllStonesAndEmptyContainer());
                }
                break;
            case PlayerTwo:
                for (int i = MancalaConstants.PlayerOneHouseIndex + 1; i < MancalaConstants.PlayerTwoHouseIndex; i++) {
                    mancalaBoard.get(MancalaConstants.PlayerTwoHouseIndex)
                            .addStones(mancalaBoard.get(i).getAllStonesAndEmptyContainer());
                }
        }
    }
}

