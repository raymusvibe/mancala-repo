package com.bol.games.mancala.controller;

import com.bol.games.mancala.controller.dto.ConnectionRequest;
import com.bol.games.mancala.exception.*;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.MancalaGameValidationService;
import com.bol.games.mancala.service.MancalaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/games/v1")
@Api(value = "Mancala game API. Set of endpoints for Creating and Validating the Game")
public class MancalaController {

    @Autowired
    private MancalaService gameService;

    @Autowired
    private MancalaGameValidationService gamePlayValidationService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/start")
    @ApiOperation(value = "Endpoint for creating new Mancala game instance. It returns a MancalaGame object with a unique GameId",
            produces = "Application/JSON", response = MancalaGame.class, httpMethod = "POST")
    public ResponseEntity<MancalaGame> start(@RequestBody Player player) throws ValidationException {
        log.info("start game request: {}", player);
        return ResponseEntity.ok(gameService.createGame(player));
    }

    @PostMapping("/connect")
    @ApiOperation(value = "Endpoint for connecting to an existing Mancala game. It returns a MancalaGame object",
            produces = "Application/JSON", response = MancalaGame.class, httpMethod = "POST")
    public ResponseEntity<MancalaGame> connect(@RequestBody ConnectionRequest request) throws InvalidGameException, IllegalRequestException, ValidationException {
        log.info("connect request: {}", request);
        return ResponseEntity.ok(gameService.connectToGame(request.getPlayer(), request.getGameId()));
    }

    @PostMapping("/connect/random")
    @ApiOperation(value = "Endpoint for connecting to a random, existing Mancala game. It returns a MancalaGame object",
            produces = "Application/JSON", response = MancalaGame.class, httpMethod = "POST")
    public ResponseEntity<MancalaGame> connectRandom(@RequestBody Player player) throws NotFoundException, ValidationException {
        log.info("connect random {}", player);
        return ResponseEntity.ok(gameService.connectToRandomGame(player));
    }

    @PostMapping("/gameplay")
    @ApiOperation(value = "Endpoint for validating front-end gameplay and notifying opponent of game changes. It returns a MancalaGame object",
            produces = "Application/JSON", response = MancalaGame.class, httpMethod = "POST")
    public ResponseEntity<MancalaGame> gamePlay(@RequestBody MancalaGame request) throws ValidationException {
        log.info("gameplay: {}", request);
        MancalaGame game = gamePlayValidationService.validate(request);
        simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
        return ResponseEntity.ok(game);
    }
}
