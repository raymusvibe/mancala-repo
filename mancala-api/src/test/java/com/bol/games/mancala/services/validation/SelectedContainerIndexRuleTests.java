package com.bol.games.mancala.services.validation;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.SelectedContainerIndexRule;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import com.bol.games.mancala.utils.DummyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class SelectedContainerIndexRuleTests {

    @Mock
    private MancalaRepository mancalaRepository;

    private static final SelectedContainerIndexRule selectedContainerIndexRule = new SelectedContainerIndexRule();
    private static final GameRule dummyRule = new DummyRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoWinMove = new ClassPathResource("playerTwoWinMove.json");
    private final Resource playerTwoNewGameMove = new ClassPathResource("playerTwoNewGameMove.json");

    @BeforeAll
    public static void setUp () {
        selectedContainerIndexRule.setSuccessor(dummyRule);
    }

    @Test
    void SelectedContainerIndexRule_WhenValidContainerSelected_NoValidationException () throws Exception {

        MancalaGame playerTwoNewGameMoveGame = mapper.readValue(resourceAsInputStream(playerTwoNewGameMove), MancalaGame.class);
        MancalaGame playerTwoWinMoveGame = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);

        assertDoesNotThrow(() -> selectedContainerIndexRule.processRequest(playerTwoNewGameMoveGame, playerTwoWinMoveGame, mancalaRepository), "ValidationException not thrown");
    }
}