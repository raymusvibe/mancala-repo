package com.bol.games.mancala.controllers;

import com.bol.games.mancala.controller.MancalaController;
import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest (classes = MancalaControllerTests.class)
@AutoConfigureJsonTesters
@ExtendWith(MockitoExtension.class)
public class MancalaControllerTests {

    @Mock
    private MancalaAPI service;

    @Mock
    private MancalaGamePlayValidationAPI validation;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<MancalaGame> mancalaJsonTestWriter;

    private String invalidGameId = "someGameId";

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MancalaController(service,
                validation,
                simpMessagingTemplate)).build();
    }

    @Test
    public void testGameCreation() throws Exception {
        MancalaGame expectedGame = new MancalaGame();
        doReturn(expectedGame).when(service).createGame();

        MockHttpServletResponse response = mockMvc
                .perform(get("/mancala/v1/start").accept(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                mancalaJsonTestWriter.write(expectedGame).getJson());
    }

    @Test
    public void testJoinGame() throws Exception {

        doThrow(new NotFoundException("Invalid GameId or this game is already in progress"))
                .when(service).connectToGame(invalidGameId);

        MockHttpServletResponse response = mockMvc
                .perform(get(String.format("/mancala/v1/connect?gameId=%s", invalidGameId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testGamePlayValidation() throws Exception {
        MancalaGame game = new MancalaGame();
        reset(validation);
        doReturn(game).when(validation).validate(any(MancalaGame.class));

        MockHttpServletResponse response = mockMvc
                .perform(post("/mancala/v1/gameplay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mancalaJsonTestWriter.write(game).getJson()))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                mancalaJsonTestWriter.write(game).getJson());
    }

    @Test
    public void testGamePlayValidationException() throws Exception {
        MancalaGame game = new MancalaGame();
        reset(validation);
        doThrow(new ValidationException("Invalid game Id provided"))
                .when(validation).validate(any(MancalaGame.class));

        //validation exception is nested
        assertThrows(Exception.class, () -> {
            mockMvc
                    .perform(post("/mancala/v1/gameplay")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mancalaJsonTestWriter.write(game).getJson()));
        }, "ValidationException was expected");
    }
}
