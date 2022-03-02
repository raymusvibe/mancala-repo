package com.bol.games.mancala.controller;

import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.service.abstractions.MancalaGameValidationAPI;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@Slf4j
@RequestMapping("/mancala/v1")
@AllArgsConstructor
public class MancalaController {

    private MancalaAPI gameService;
    private MancalaGameValidationAPI validationService;
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping(value = "/start", produces = "application/json")
    @Operation(summary = "Start a new game")
    public ResponseEntity<MancalaGame> start() throws Exception {
        log.info("start game request");
        MancalaGame game = (MancalaGame) gameService.createGame();
        return ResponseEntity.ok(game);
    }

    @GetMapping (value = "/connect", produces = "application/json")
    @Operation(summary = "Connect to already existing game using a gameId")
    public ResponseEntity<MancalaGame> connect(@RequestParam String gameId) throws Exception {
        log.info("connect request: {}", gameId);
        MancalaGame game = (MancalaGame) gameService.connectToGame(gameId);
        return ResponseEntity.ok(game);
    }

    @PostMapping(value = "/gameplay", produces = "application/json", consumes = "application/json")
    @Operation(summary = "Gameplay validation and publishing game state to opponent")
    public ResponseEntity<MancalaGame> gamePlay(@RequestBody MancalaGame request) throws Exception {
        log.info("gameplay: {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request));
        MancalaGame game = validationService.validate(request);
        simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
        return ResponseEntity.ok(game);
    }
}
