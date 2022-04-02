package com.bol.games.mancala.controller;

import com.bol.games.mancala.controller.dto.Message;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class MancalaWebSocketController {

    @Autowired
    private MancalaGamePlayValidationAPI validationService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/gameplay.{gameId}")
    public MancalaGame gamePlay (@Payload MancalaGame request, @DestinationVariable("gameId") String gameId) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("gameplay: {}", gameId);
        }
        MancalaGame game = validationService.validate(request);
        simpMessagingTemplate.convertAndSend("/topic/game-progress." + gameId, game);
        return game;
    }

    @MessageMapping("/messaging.{gameId}")
    public Message chat (@Payload Message message, @DestinationVariable("gameId") String gameId) {
        if (log.isInfoEnabled()) {
            log.info("chat: {}", gameId);
        }
        simpMessagingTemplate.convertAndSend("/topic/game-messaging." + gameId, message);
        return message;
    }
}
