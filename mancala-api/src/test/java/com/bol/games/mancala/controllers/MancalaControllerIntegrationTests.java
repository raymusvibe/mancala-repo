package com.bol.games.mancala.controllers;

import com.bol.games.mancala.controller.MancalaRestController;
import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest (classes = MancalaControllerIntegrationTests.class)
@AutoConfigureJsonTesters
@ExtendWith(MockitoExtension.class)
class MancalaControllerIntegrationTests {
    @InjectMocks
    MancalaRestController mancalaController;
    @Mock
    private MancalaAPI service;
    @Autowired
    private JacksonTester<MancalaGame> jsonTestWriter;

    private MockMvc mockMvc;
    private static final String invalidGameId = "someGameId";

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mancalaController).build();
    }

    @Test
    void MancalaController_WhenNewGameRequest_CreatesNewGame() throws Exception {
        MancalaGame expectedGame = new MancalaGame(null);
        doReturn(expectedGame).when(service).createGame();

        MockHttpServletResponse response = mockMvc
                .perform(get("/mancala/v1/start").accept(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        assertThat(response.getContentAsString()).isEqualTo(
                jsonTestWriter.write(expectedGame).getJson());
    }

    @Test
    void MancalaController_WhenInvalidGameId_NotFoundException() throws Exception {

        doThrow(new NotFoundException("Invalid GameId or this game is already in progress"))
                .when(service).connectToGame(invalidGameId);

        assertThrows(Exception.class, () -> mockMvc.perform(get(String.format("/mancala/v1/connect?gameId=%s", invalidGameId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse(), "NotFoundException was expected");
    }
}
