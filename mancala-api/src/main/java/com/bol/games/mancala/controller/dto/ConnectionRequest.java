package com.bol.games.mancala.controller.dto;

import com.bol.games.mancala.model.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class ConnectionRequest {
    private @Getter @Setter Player player;
    private @Getter @Setter String gameId;
}
