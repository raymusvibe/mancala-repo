package com.bol.games.mancala.model;

import com.bol.games.mancala.constants.MancalaConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
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

    public MancalaGame (String gameId) {
        this.gameId = (gameId == null)? UUID.randomUUID().toString() : gameId;
        activePlayer = Player.PLAYER_ONE;
        gamePlayStatus = GameStatus.NEW;
        selectedStoneContainerIndex = MancalaConstants.PLAYER_ONE_HOUSE_INDEX;
    }

    @JsonIgnore
    public final void initialiseBoardToNewGame() {
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

    @JsonIgnore
    public final StoneContainer getNextContainerSowed(Integer currentIndex) {
        int nextIndex = currentIndex;
        //cannot place stones on another player's house
        if (isOpponentHouse(nextIndex)) {
            //avoid array out of bounds error
            nextIndex = (nextIndex + 1) % (MancalaConstants.PLAYER_TWO_HOUSE_INDEX + 1);
        }
        return mancalaBoard.get(nextIndex);
    }

    @JsonIgnore
    public final Optional<MancalaGame> finishGame() {
        Optional<MancalaGame> finishedGame = Optional.empty();
        if (isGameFinished()) {
            int playerOneFinalStoneCount = getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones();
            int playerTwoFinalStoneCount = getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones();
            if (playerOneFinalStoneCount > playerTwoFinalStoneCount) {
                setWinner(GameWinner.PLAYER_ONE);
            } else if (playerOneFinalStoneCount < playerTwoFinalStoneCount) {
                setWinner(GameWinner.PLAYER_TWO);
            } else {
                setWinner(GameWinner.DRAW);
            }
            setGamePlayStatus(GameStatus.FINISHED);
            finishedGame = Optional.of(this);
        }
        return finishedGame;
    }

    @JsonIgnore
    public final boolean isNotMyContainer (int targetContainerIndex) {
        boolean isNotMyContainer = false;
        if (activePlayer == Player.PLAYER_ONE) {
            if (targetContainerIndex > MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isNotMyContainer = true;
            }
        } else {
            if (targetContainerIndex <= MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isNotMyContainer = true;
            }
        }
        return isNotMyContainer;
    }

    @JsonIgnore
    public final void movePlayerStonesToHouse (Player player) {
        if (player == Player.PLAYER_ONE) {
            for (int i = 0; i < MancalaConstants.PLAYER_ONE_HOUSE_INDEX; i++) {
                mancalaBoard.get(MancalaConstants.PLAYER_ONE_HOUSE_INDEX)
                        .addStones(mancalaBoard.get(i).getAllStonesAndEmptyContainer());
            }
        } else {
            for (int i = MancalaConstants.PLAYER_ONE_HOUSE_INDEX + 1; i < MancalaConstants.PLAYER_TWO_HOUSE_INDEX; i++) {
                mancalaBoard.get(MancalaConstants.PLAYER_TWO_HOUSE_INDEX)
                        .addStones(mancalaBoard.get(i).getAllStonesAndEmptyContainer());
            }
        }
    }

    private boolean isOpponentHouse (Integer containerIndex) {
        boolean isOpponentHouse = false;
        if (activePlayer == Player.PLAYER_ONE) {
            if (containerIndex == MancalaConstants.PLAYER_TWO_HOUSE_INDEX) {
                isOpponentHouse = true;
            }
        } else {
            if (containerIndex == MancalaConstants.PLAYER_ONE_HOUSE_INDEX) {
                isOpponentHouse = true;
            }
        }
        return isOpponentHouse;
    }

    private boolean isGameFinished () {
        boolean isFinished = false;
        int playerOneStoneCount = MancalaConstants.EMPTY_STONE_COUNT;
        for (int i = 0; i < MancalaConstants.PLAYER_ONE_HOUSE_INDEX; i++) {
            playerOneStoneCount += getStoneContainer(i).getStones();
        }
        if (playerOneStoneCount == MancalaConstants.EMPTY_STONE_COUNT) {
            isFinished = true;
            movePlayerStonesToHouse(Player.PLAYER_TWO);
        } else {
            int playerTwoStoneCount = MancalaConstants.EMPTY_STONE_COUNT;
            for (int i = MancalaConstants.PLAYER_ONE_HOUSE_INDEX + 1; i < MancalaConstants.PLAYER_TWO_HOUSE_INDEX; i++) {
                playerTwoStoneCount += getStoneContainer(i).getStones();
            }
            if (playerTwoStoneCount == MancalaConstants.EMPTY_STONE_COUNT) {
                isFinished = true;
                movePlayerStonesToHouse(Player.PLAYER_ONE);
            }
        }
        return isFinished;
    }
}

