package com.bol.games.mancala.service;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
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

import static com.bol.games.mancala.util.TestUtils.resourceAsInputStream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MancalaGameValidationServiceIntegrationTests {

    private MancalaGamePlayValidationService validationService;
    @Mock
    private MongoTemplate mancalaGamesMongoTemplate;
    @Mock
    private MongoTemplate mancalaEventsMongoTemplate;

    private static final MancalaGame newGame = new MancalaGame(null);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource playerOneHouseIndexSelected = new ClassPathResource("playerOneHouseSelectedMove.json");
    private final Resource playerOneFirstMove = new ClassPathResource("playerOneFirstMove.json");
    private final Resource playerOneFirstMoveInvalidStoneCount = new ClassPathResource("playerOneFirstMoveInvalidStoneCountMove.json");
    private final Resource playerOneSecondMove = new ClassPathResource("playerOneSecondMove.json");
    private final Resource playerTwoWinMove = new ClassPathResource("playerTwoWinMove.json");
    private final Resource playerTwoWinInvalidStoneCountMove = new ClassPathResource("playerTwoWinInvalidStoneCountMove.json");
    private final Resource playerTwoWinPriorMove = new ClassPathResource("playerTwoWinPriorMove.json");
    private final Resource playerTwoOppositeStoneCaptureMove = new ClassPathResource("playerTwoOppositeStoneCaptureMove.json");
    private final Resource playerTwoOppositeStoneCapturePriorMove = new ClassPathResource("playerTwoOppositeStoneCapturePriorMove.json");
    private final Resource playerOneOpponentHouseSkipPriorMove = new ClassPathResource("playerOneOpponentHouseSkipPriorMove.json");
    private final Resource playerOneOpponentHouseSkipMove = new ClassPathResource("playerOneOpponentHouseSkipMove.json");

    @BeforeAll
    public static void baseSetUp () {
        newGame.initialiseBoardToNewGame();
        newGame.setGamePlayStatus(GameStatus.IN_PROGRESS);
    }

    @BeforeEach
    public void setUp () {
        MancalaRepository mancalaRepository = new MancalaRepository(mancalaGamesMongoTemplate, mancalaEventsMongoTemplate);
        validationService = new MancalaGamePlayValidationService(mancalaRepository);
    }

    @Test
    void ValidationService_WhenHouseIndexSelected_NoFurtherActionRequired () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneHouseIndexSelected), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void ValidationService_WhenLastStoneInHouse_PlayerPlaysAgain () throws Exception {
        doReturn(newGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());
        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneFirstMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(newGame.getActivePlayer());
    }

    @Test
    void ValidationService_WhenInvalidStoneCount_ReturnCorrectState () throws Exception {
        MancalaGame playerOneFirstMoveGame = mapper.readValue(resourceAsInputStream(playerOneFirstMove), MancalaGame.class);
        doReturn(playerOneFirstMoveGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneFirstMoveInvalidStoneCount), MancalaGame.class);
        MancalaGame validatedGame = validationService.validate(gameFromFrontEnd);

        assertThat(validatedGame.getGamePlayStatus()).isEqualTo(playerOneFirstMoveGame.getGamePlayStatus());
        assertThat(validatedGame.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones())
                .isEqualTo(playerOneFirstMoveGame.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones());
        assertThat(validatedGame.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones())
                .isEqualTo(playerOneFirstMoveGame.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones());
        assertThat(validatedGame.getWinner()).isNull();
    }

    @Test
    void ValidationService_WhenLastStoneNotInHouse_TurnChanges () throws Exception {
        MancalaGame playerOneFirstMoveGame = mapper.readValue(resourceAsInputStream(playerOneFirstMove), MancalaGame.class);
        doReturn(playerOneFirstMoveGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneSecondMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isNotEqualByComparingTo(gameFromFrontEnd.getActivePlayer());
    }

    @Test
    void ValidationService_WhenPlayerWinsGenuinely_WinValidated () throws Exception {
        MancalaGame playerTwoWinPriorGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);
        doReturn(playerTwoWinPriorGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerTwoWinMove), MancalaGame.class);
        MancalaGame validatedGame = validationService.validate(gameFromFrontEnd);

        assertThat(validatedGame.getWinner()).isNotNull();
        assertThat(validatedGame.getGamePlayStatus()).isEqualTo(gameFromFrontEnd.getGamePlayStatus());
    }

    @Test
    void ValidationService_WhenPlayerWinsWithInvalidStoneCount_ReturnCorrectState () throws Exception {
        MancalaGame playerTwoWinPriorGame = mapper.readValue(resourceAsInputStream(playerTwoWinPriorMove), MancalaGame.class);
        doReturn(playerTwoWinPriorGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerTwoWinInvalidStoneCountMove), MancalaGame.class);
        MancalaGame validatedGame = validationService.validate(gameFromFrontEnd);

        assertThat(validatedGame.getGamePlayStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(validatedGame.getWinner()).isNull();
    }

    @Test
    void ValidationService_WhenOppositeStoneCaptured_TurnChanges () throws Exception {
        MancalaGame playerTwoOppositeStoneCapturePriorGame = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCapturePriorMove), MancalaGame.class);
        doReturn(playerTwoOppositeStoneCapturePriorGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerTwoOppositeStoneCaptureMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }

    @Test
    void ValidationService_WhenOpponentHouseSkipped_HouseStoneCountEqual () throws Exception {
        MancalaGame playerOneOpponentHouseSkipPriorMoveGame = mapper.readValue(resourceAsInputStream(playerOneOpponentHouseSkipPriorMove), MancalaGame.class);
        doReturn(playerOneOpponentHouseSkipPriorMoveGame).when(mancalaGamesMongoTemplate).findOne(any(Query.class), ArgumentMatchers.<Class<MancalaGame>>any());

        MancalaGame gameFromFrontEnd = mapper.readValue(resourceAsInputStream(playerOneOpponentHouseSkipMove), MancalaGame.class);
        MancalaGame validatedGame = validationService.validate(gameFromFrontEnd);

        assertThat(validatedGame.getGameId()).isEqualTo(gameFromFrontEnd.getGameId());
        assertThat(validatedGame.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones())
                .isEqualTo(gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_ONE_HOUSE_INDEX).getStones());
        assertThat(validatedGame.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones())
                .isEqualTo(gameFromFrontEnd.getStoneContainer(MancalaConstants.PLAYER_TWO_HOUSE_INDEX).getStones());
    }
}
