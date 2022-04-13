package com.bol.games.mancala.controller;

import com.bol.games.mancala.util.TestMessageChannel;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.service.MancalaGamePlayValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Arrays;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class MancalaWebSocketControllerIntegrationTests {
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private MancalaRepository mancalaRepository;
    @InjectMocks
    private MancalaGamePlayValidationService validationService;
    private TestMessageChannel clientOutboundChannel;
    private TestAnnotationMethodHandler annotationMethodHandler;
    private final MancalaGame game = new MancalaGame(null);

    @BeforeEach
    public void setup() {
        MancalaWebSocketController controller = new MancalaWebSocketController(validationService, simpMessagingTemplate);
        clientOutboundChannel = new TestMessageChannel();
        annotationMethodHandler = new TestAnnotationMethodHandler(
                new TestMessageChannel(), clientOutboundChannel, new SimpMessagingTemplate(new TestMessageChannel()));
        annotationMethodHandler.registerHandler(controller);
        annotationMethodHandler.setDestinationPrefixes(Arrays.asList("/app"));
        annotationMethodHandler.setMessageConverter(new MappingJackson2MessageConverter());
        annotationMethodHandler.setApplicationContext(new StaticApplicationContext());
        annotationMethodHandler.afterPropertiesSet();
    }

    @Test
    public void WebSocketController_WhenChatMessage_RoutesWithoutException() throws Exception {
        com.bol.games.mancala.controller.dto.Message text = new com.bol.games.mancala.controller.dto.Message("Hello", "Test");
        byte[] payload = new ObjectMapper().writeValueAsBytes(text);
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/app/messaging.some-game-id");
        headers.setSessionId("0");
        headers.setSessionAttributes(new HashMap<>());
        Message<byte[]> message = MessageBuilder.withPayload(payload).setHeaders(headers).build();
        assertDoesNotThrow(() -> {
            this.annotationMethodHandler.handleMessage(message);
        }, "Exception not thrown");
    }

    @Test
    public void WebSocketController_WhenGamePlay_RoutesWithoutException() throws Exception {
        game.initialiseBoardToNewGame();
        game.setGamePlayStatus(GameStatus.IN_PROGRESS);
        doReturn(game).when(mancalaRepository).findGame(any(String.class));

        byte[] payload = new ObjectMapper().writeValueAsBytes(game);

        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/app/gameplay." + game.getGameId());
        headers.setSessionId("0");
        headers.setSessionAttributes(new HashMap<>());
        Message<byte[]> message = MessageBuilder.withPayload(payload).setHeaders(headers).build();

        this.annotationMethodHandler.handleMessage(message);
        MancalaGame validatedGame = validationService.validate(game);
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
