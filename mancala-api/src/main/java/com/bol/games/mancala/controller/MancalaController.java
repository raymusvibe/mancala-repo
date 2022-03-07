package com.bol.games.mancala.controller;

import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/mancala/v1")
@AllArgsConstructor
public class MancalaController {

    @Autowired
    private MancalaAPI mancalaService;
    @Autowired
    private MancalaGamePlayValidationAPI validationService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @GetMapping(value = "/start", produces = "application/json")
    @Operation(summary = "Start a new game")
    public ResponseEntity<MancalaGame> start() {
        if (log.isInfoEnabled()) {
            log.info("start game request");
        }
        MancalaGame game = mancalaService.createGame();
        return ResponseEntity.ok(game);
    }

    @GetMapping (value = "/connect", produces = "application/json")
    @Operation(summary = "Connect to already existing game using a gameId")
    public ResponseEntity<MancalaGame> connect(@RequestParam String gameId) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("connect request: {}", gameId);
        }
        MancalaGame game = mancalaService.connectToGame(gameId);
        return ResponseEntity.ok(game);
    }

    @PostMapping(value = "/gameplay", produces = "application/json", consumes = "application/json")
    @Operation(summary = "Gameplay validation and publishing game state to opponent")
    public ResponseEntity<MancalaGame> gamePlay(@RequestBody MancalaGame request) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("gameplay: {}", request.getGameId());
        }
        MancalaGame game = validationService.validate(request);
        simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
        return ResponseEntity.ok(game);
    }
}
