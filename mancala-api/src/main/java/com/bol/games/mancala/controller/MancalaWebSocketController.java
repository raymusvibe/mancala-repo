package com.bol.games.mancala.controller;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.controller.dto.Message;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@AllArgsConstructor
public class MancalaWebSocketController {

    @Autowired
    private MancalaGamePlayAPI gamePlayService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @MessageMapping("/gameplay.{gameId}")
    public MancalaGame gamePlay (@Payload GamePlay request, @DestinationVariable("gameId") String gameId) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("gameplay: {}", mapper.writeValueAsString(request));
        }
        MancalaGame game = gamePlayService.executeGameRules(request);
        simpMessagingTemplate.convertAndSend("/topic/game-progress." + gameId, game);
        return game;
    }

    @MessageMapping("/messaging.{gameId}")
    public Message chat (@Payload Message chatMessage, @DestinationVariable("gameId") String gameId) {
        if (log.isInfoEnabled()) {
            log.info("chat: {}", gameId);
        }
        simpMessagingTemplate.convertAndSend("/topic/game-messaging." + gameId, chatMessage);
        return chatMessage;
    }
}
