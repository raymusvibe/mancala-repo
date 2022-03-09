package com.bol.games.mancala.services.validation;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.validation.GameExistsInStoreRule;
import com.bol.games.mancala.service.validation.abstractions.GameRule;
import com.bol.games.mancala.utils.DummyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static com.bol.games.mancala.utils.TestUtils.resourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GameExistsInStoreRuleUnitTests {

    private MancalaRepository mancalaRepository;
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    private final GameExistsInStoreRule gameExistsInStoreRule = new GameExistsInStoreRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerTwoOppositeStoneCaptureMove = new ClassPathResource("test/playerTwoOppositeStoneCaptureMove.json");
    private final Resource playerTwoOppositeStoneCapturePriorMove = new ClassPathResource("test/playerTwoOppositeStoneCapturePriorMove.json");

    @BeforeEach
    public void setUp () {
        GameRule dummyRule = new DummyRule();
        gameExistsInStoreRule.setSuccessor(dummyRule);
        //issues with mock injection
        mancalaRepository = new MancalaRepository(mancalaGamesMongoTemplate, mancalaEventsMongoTemplate);
    }

    @Test
    void GameExistsInStoreRule_WhenIncorrectGameId_ValidationException () throws Exception {
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);

        assertThrows(ValidationException.class, () -> gameExistsInStoreRule
                .processRequest(playerTwoOppositeStoneCaptureGame,
                        playerTwoOppositeStoneCapturePriorGame,
                        mancalaRepository),
                "ValidationException was expected");
    }

    @Test
    void GameExistsInStoreRule_WhenValidGameId_NoValidationException () throws Exception {
        MancalaGame playerTwoOppositeStoneCaptureGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);

        doReturn(playerTwoOppositeStoneCapturePriorGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        assertDoesNotThrow(() -> gameExistsInStoreRule
                .processRequest(playerTwoOppositeStoneCaptureGame,
                        playerTwoOppositeStoneCapturePriorGame,
                        mancalaRepository),
                "ValidationException not thrown");
    }
}
