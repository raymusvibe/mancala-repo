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
        activePlayer = Player.PLAYER_ONE;
        gameId = UUID.randomUUID().toString();
        gamePlayStatus = GameStatus.NEW;
    }

    public void initialiseBoard() {
        ArrayList <StoneContainer> stoneContainers = new ArrayList<>();
        Integer totalNumberOfContainers = (MancalaConstants.CONTAINERS_PER_PLAYER + 1) * 2;
        for (int i = 0; i < totalNumberOfContainers; i++) {
            if (i != MancalaConstants.PLAYER_ONE_HOUSE_INDEX &&
                    i != MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                stoneContainers.add(new StoneContainer(i, MancalaConstants.STONES_PER_CONTAINER));
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
        for (int i = 0; i < MancalaConstants.PLAYER_ONE_HOUSE_INDEX; i++)
            playerOneStoneCount += getStoneContainer(i).getStones();
        if (playerOneStoneCount == 0) {
            movePlayerStonesToHouse(Player.PLAYER_TWO);
        } else {
            int playerTwoStoneCount = 0;
            for (int i = MancalaConstants.PLAYER_ONE_HOUSE_INDEX + 1; i < MancalaConstants.PLAYER_TWO_HOUSE_INDEX; i++)
                playerTwoStoneCount += getStoneContainer(i).getStones();
            if (playerTwoStoneCount == 0)
                movePlayerStonesToHouse(Player.PLAYER_ONE);
            else
                //game isn't finished
                return Optional.empty();
        }

        //Game has ended
        int playerOneFinalStoneCount = getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
        int playerTwoFinalStoneCount = getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();

        if (playerOneFinalStoneCount > playerTwoFinalStoneCount)
            setWinner(GameWinner.PLAYER_ONE);
        else if (playerOneFinalStoneCount < playerTwoFinalStoneCount)
            setWinner(GameWinner.PLAYER_TWO);
        else
            setWinner(GameWinner.DRAW);

        setGamePlayStatus(GameStatus.FINISHED);

        return Optional.of(this);
    }

    @JsonIgnore
    private void movePlayerStonesToHouse (Player player) {
        switch (player) {
            case PLAYER_ONE:
                for (int i = 0; i < MancalaConstants.PLAYER_ONE_HOUSE_INDEX; i++) {
                    mancalaBoard.get(MancalaConstants.PLAYER_ONE_HOUSE_INDEX)
                            .addStones(mancalaBoard.get(i).getAllStonesAndEmptyContainer());
                }
                break;
            case PLAYER_TWO:
                for (int i = MancalaConstants.PLAYER_ONE_HOUSE_INDEX + 1; i < MancalaConstants.PLAYER_TWO_HOUSE_INDEX; i++) {
                    mancalaBoard.get(MancalaConstants.PLAYER_TWO_HOUSE_INDEX)
                            .addStones(mancalaBoard.get(i).getAllStonesAndEmptyContainer());
                }
        }
    }
}

