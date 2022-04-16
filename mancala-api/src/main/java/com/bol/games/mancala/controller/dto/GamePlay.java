package com.bol.games.mancala.controller.dto;

import com.bol.games.mancala.model.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
/*
  Used to communicate game events from the frontend/client,
  such as a game restart request or to update the game
  object after a player's turn.
  */
public class GamePlay {
    private String gameId;
    private GameStatus gamePlayStatus;
    private Integer selectedStoneContainerIndex;
}
