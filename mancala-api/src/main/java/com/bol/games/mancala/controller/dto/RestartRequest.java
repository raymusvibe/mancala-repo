package com.bol.games.mancala.controller.dto;

import com.bol.games.mancala.model.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class RestartRequest {
    private @Getter @Setter Player playerOne;
    private @Getter @Setter Player playerTwo;
}