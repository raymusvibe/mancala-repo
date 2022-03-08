package com.bol.games.mancala.services;

import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.MancalaGamePlayValidationService;
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
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MancalaGameValidationServiceTests {

    private MancalaGamePlayValidationService validationService;
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    private final MancalaGame newGame = new MancalaGame();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerOneHouseIndexSelected = new ClassPathResource("test/playerOneHouseSelectedMove.json");
    private final Resource playerOneFirstMove = new ClassPathResource("test/playerOneFirstMove.json");
    private final Resource playerOneFirstMoveInvalidStoneCount = new ClassPathResource("test/playerOneFirstMoveInvalidStoneCountMove.json");
    private final Resource playerOneSecondMove = new ClassPathResource("test/playerOneSecondMove.json");
    private final Resource playerTwoFirstMove = new ClassPathResource("test/playerTwoFirstMove.json");
    private final Resource playerTwoWinMove = new ClassPathResource("test/playerTwoWinMove.json");
    private final Resource playerTwoWinInvalidStoneCountMove = new ClassPathResource("test/playerTwoWinInvalidStoneCountMove.json");
    private final Resource playerTwoWinPriorMove = new ClassPathResource("test/playerTwoWinPriorMove.json");
    private final Resource playerTwoOppositeStoneCaptureMove = new ClassPathResource("test/playerTwoOppositeStoneCaptureMove.json");
    private final Resource playerTwoOppositeStoneCapturePriorMove = new ClassPathResource("test/playerTwoOppositeStoneCapturePriorMove.json");
    private final Resource playerOneOpponentHouseSkipPriorMove = new ClassPathResource("test/playerOneOpponentHouseSkipPriorMove.json");
    private final Resource playerOneOpponentHouseSkipMove = new ClassPathResource("test/playerOneOpponentHouseSkipMove.json");

    @BeforeEach
    public void setUp () {
        newGame.initialiseBoard();
        newGame.setGamePlayStatus(GameStatus.IN_PROGRESS);
        MancalaRepository mancalaRepository = new MancalaRepository(mancalaGamesMongoTemplate, mancalaEventsMongoTemplate);
        validationService = new MancalaGamePlayValidationService(mancalaRepository);
    }

    @Test
    void testValidationPlayerOneHouseIndexSelection () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneHouseIndexSelected), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void testValidationPlayerOneFirstMove () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());
        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneFirstMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void testValidationPlayerOneFirstMoveStoneCountInvalid () throws Exception {
        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneFirstMoveInvalidStoneCount), MancalaGame.class);

        assertThrows(ValidationException.class, () -> validationService.validate(gameFromFrontEnd),
                "ValidationException was expected");
    }

    @Test
    void testValidationPlayerOneSecondMove () throws Exception {
        MancalaGame playerOneFirstMoveGame = mapper.readValue(resourceAsInputStream(this.playerOneFirstMove), MancalaGame.class);
        doReturn(playerOneFirstMoveGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneSecondMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_TWO);
    }

    @Test
    void testValidationPlayerTwoFirstMove () throws Exception {
        MancalaGame playerOneSecondMoveGame = mapper.readValue(resourceAsInputStream(playerOneSecondMove), MancalaGame.class);
        playerOneSecondMoveGame.setActivePlayer(Player.PLAYER_TWO);
        doReturn(playerOneSecondMoveGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerTwoFirstMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void testValidationWin () throws Exception {
        MancalaGame playerTwoWinPriorGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);
        doReturn(playerTwoWinPriorGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);

        assertDoesNotThrow(() -> {
            validationService.validate(gameFromFrontEnd);
        }, "ValidationException not thrown");
    }

    @Test
    void testValidationWinInvalidStoneCount () throws Exception {
        MancalaGame playerTwoWinPriorGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);
        doReturn(playerTwoWinPriorGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerTwoWinInvalidStoneCountMove), MancalaGame.class);

        assertThrows(ValidationException.class, () -> validationService
                .validate(gameFromFrontEnd),
                "ValidationException was expected");
    }

    @Test
    void testValidationOppositeStoneCapture () throws Exception {
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);
        doReturn(playerTwoOppositeStoneCapturePriorGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void testValidationOpponentHouseSkip () throws Exception {
        MancalaGame playerOneOpponentHouseSkipPriorMoveGame = mapper.readValue(resourceAsInputStream(playerOneOpponentHouseSkipPriorMove), MancalaGame.class);
        doReturn(playerOneOpponentHouseSkipPriorMoveGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneOpponentHouseSkipMove), MancalaGame.class);

        assertDoesNotThrow(() -> {
            validationService.validate(gameFromFrontEnd);
        }, "ValidationException not thrown");
    }
}
