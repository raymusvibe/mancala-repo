package com.bol.games.mancala.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
public class WebsocketEventListener {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Received a new web socket connection: " + event.toString());
        }
    }

    @EventListener
    public void handleWebsocketConnect(SessionConnectEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Connection event: " + event.toString());
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Subscribe event: " + event.toString());
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Unsubscribe event: " + event.toString());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Websocket connection " + event.getSessionId() + " terminated - " + event.toString());
        }
    }
}
