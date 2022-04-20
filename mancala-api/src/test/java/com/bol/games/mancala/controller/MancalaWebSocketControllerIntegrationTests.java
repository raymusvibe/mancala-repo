package com.bol.games.mancala.controller;

import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.controller.dto.Message;
import com.bol.games.mancala.service.MancalaGamePlayService;
import com.bol.games.mancala.util.TestMessageChannel;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.repository.MancalaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MancalaWebSocketControllerIntegrationTests {
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private MancalaRepository mancalaRepository;
    @InjectMocks
    private MancalaGamePlayService gamePlayService;
    private TestAnnotationMethodHandler annotationMethodHandler;
    private final MancalaGame game = new MancalaGame();

    @BeforeEach
    public void setup() {
        MancalaWebSocketController controller = new MancalaWebSocketController(gamePlayService, simpMessagingTemplate);
        TestMessageChannel clientOutboundChannel = new TestMessageChannel();
        annotationMethodHandler = new TestAnnotationMethodHandler(
                new TestMessageChannel(), clientOutboundChannel, new SimpMessagingTemplate(new TestMessageChannel()));
        annotationMethodHandler.registerHandler(controller);
        annotationMethodHandler.setDestinationPrefixes(List.of("/app"));
        annotationMethodHandler.setMessageConverter(new MappingJackson2MessageConverter());
        annotationMethodHandler.setApplicationContext(new StaticApplicationContext());
        annotationMethodHandler.afterPropertiesSet();
    }

    @Test
    void WebSocketController_WhenChatMessage_RoutesWithoutException() throws Exception {
        Message text = new Message("Hello", "Test");
        byte[] payload = new ObjectMapper().writeValueAsBytes(text);
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/app/messaging.some-game-id");
        headers.setSessionId("0");
        headers.setSessionAttributes(new HashMap<>());
        org.springframework.messaging.Message<byte[]> message = MessageBuilder.withPayload(payload).setHeaders(headers).build();
        assertDoesNotThrow(() -> this.annotationMethodHandler.handleMessage(message), "Exception not thrown");
    }

    @Test
    void WebSocketController_WhenGamePlay_ServiceExecutesRules() throws Exception {
        game.initialiseBoardToStartNewGame();
        game.setGamePlayStatus(GameStatus.IN_PROGRESS);
        doReturn(game).when(mancalaRepository).findGame(any(String.class));

        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.IN_PROGRESS, 0);

        byte[] payload = new ObjectMapper().writeValueAsBytes(gamePlay);

        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/app/gameplay." + gamePlay.getGameId());
        headers.setSessionId("0");
        headers.setSessionAttributes(new HashMap<>());
        org.springframework.messaging.Message<byte[]> message = MessageBuilder.withPayload(payload).setHeaders(headers).build();

        this.annotationMethodHandler.handleMessage(message);
        MancalaGame validatedGame = gamePlayService.executeGameRules(gamePlay);
        assertThat(validatedGame.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(validatedGame.getGamePlayStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    private static class TestAnnotationMethodHandler extends SimpAnnotationMethodMessageHandler {
        public TestAnnotationMethodHandler(SubscribableChannel inChannel, MessageChannel outChannel,
                                           SimpMessageSendingOperations brokerTemplate) {
            super(inChannel, outChannel, brokerTemplate);
        }
        public void registerHandler(Object handler) {
            super.detectHandlerMethods(handler);
        }
    }
}
