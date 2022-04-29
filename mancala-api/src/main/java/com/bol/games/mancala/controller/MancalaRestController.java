package com.bol.games.mancala.controller;

import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/mancala/v1")
@AllArgsConstructor
public class MancalaRestController {

    @Autowired
    private MancalaAPI mancalaService;

    @GetMapping(value = "/start", produces = "application/json")
    @Operation(summary = "Start a new game")
    public ResponseEntity<MancalaGame> start () {
        if (log.isInfoEnabled()) {
            log.info("start game request");
        }
        MancalaGame game = mancalaService.createGame();
        return ResponseEntity.ok(game);
    }

    @GetMapping (value = "/connect", produces = "application/json")
    @Operation(summary = "Connect to already existing game using a gameId")
    public ResponseEntity<MancalaGame> connect (@RequestParam String gameId) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("connect request: {}", gameId);
        }
        MancalaGame game = mancalaService.connectToGame(gameId);
        return ResponseEntity.ok(game);
    }
}
