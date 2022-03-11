package com.bol.games.mancala.contract;

import com.bol.games.mancala.controller.MancalaController;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.service.abstractions.MancalaGamePlayValidationAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class BaseClass {
    @Autowired
    MancalaController mancalaController;
    @Mock
    MancalaAPI mancalaService;
    @Mock
    MancalaGamePlayValidationAPI mancalaValidationService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerOneFirstMove = new ClassPathResource("playerOneFirstMove.json");
    private static final String contractTestGameId = "3d7cdee7-b2f7-4b63-9d5f-55f262f6737f";

    @BeforeEach
    public void setup() throws Exception {
        RestAssuredMockMvc.standaloneSetup(mancalaController);
        MancalaGame newGame = new MancalaGame(contractTestGameId);
        doReturn(newGame).when(mancalaService).createGame();
        MancalaGame newConnectedGame = new MancalaGame(contractTestGameId);
        newConnectedGame.setGamePlayStatus(GameStatus.IN_PROGRESS);
        doReturn(newConnectedGame).when(mancalaService).connectToGame(contractTestGameId);
        MancalaGame firstMove = mapper.readValue(resourceAsInputStream(playerOneFirstMove), MancalaGame.class);
        doReturn(firstMove).when(mancalaValidationService).validate(firstMove);
    }
}
