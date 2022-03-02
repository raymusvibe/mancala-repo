package com.bol.games.mancala.services;

import com.bol.games.mancala.data.MancalaRepository;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.model.Player;
import com.bol.games.mancala.service.MancalaGameValidationService;
import com.bol.games.mancala.service.abstractions.MancalaGameValidationAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MancalaGameValidationServiceTests {

    @Mock
    MancalaRepository mancalaRepository;

    private MancalaGame newGame;
    private MancalaGameValidationAPI validationService;
    private ObjectMapper mapper = new ObjectMapper();

    private final Resource playerOneHouseIndexSelected = new ClassPathResource("test/playerOneHouseSelected.json");
    private final Resource playerOneFirstMove = new ClassPathResource("test/playerOneFirstMove.json");
    private final Resource playerOneFirstMoveInvalidStoneCount = new ClassPathResource("test/playerOneFirstMoveInvalidStoneCount.json");
    private final Resource playerOneSecondMove = new ClassPathResource("test/playerOneSecondMove.json");
    private final Resource playerTwoFirstMove = new ClassPathResource("test/playerTwoFirstMove.json");
    private final Resource playerTwoWinner = new ClassPathResource("test/playerTwoWinner.json");
    private final Resource playerTwoWinnerInvalidStoneCount = new ClassPathResource("test/playerTwoWinnerInvalidStoneCount.json");
    private final Resource playerTwoWinnerPenultimate = new ClassPathResource("test/playerTwoWinnerPenultimate.json");
    private final Resource playerTwoStoneCapture = new ClassPathResource("test/playerTwoOppositeStoneCapture.json");
    private final Resource playerTwoStoneCapturePenultimate = new ClassPathResource("test/playerTwoOppositeStoneCapturePenultimate.json");

    @BeforeEach
    public void setUp () {
        newGame = new MancalaGame();
        newGame.setGamePlayStatus(GameStatus.IN_PROGRESS);
        validationService = new MancalaGameValidationService(mancalaRepository);
    }

    private Reader asInputStream (Resource resource) throws IOException {
        return new InputStreamReader(resource.getInputStream(), UTF_8);
    }

    @Test
    public void testValidationPlayerOneHouseIndexSelection () throws Exception {
        doReturn(Optional.of(newGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerOneHouseIndexSelected), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getMancalaBoard().toString())
                .isEqualTo("[0:6, 1:6, 2:6, 3:6, 4:6, 5:6, 6:0, 7:6, 8:6, 9:6, 10:6, 11:6, 12:6, 13:0]");
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(validationResult.getWinner()).isNull();
    }

    @Test
    public void testValidationPlayerOneFirstMove () throws Exception {
        doReturn(Optional.of(newGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerOneFirstMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getMancalaBoard().toString())
                .isEqualTo("[0:0, 1:7, 2:7, 3:7, 4:7, 5:7, 6:1, 7:6, 8:6, 9:6, 10:6, 11:6, 12:6, 13:0]");
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(validationResult.getWinner()).isNull();
    }

    @Test
    public void testValidationPlayerOneFirstMoveStoneCountInvalid () throws Exception {
        doReturn(Optional.of(newGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerOneFirstMoveInvalidStoneCount), MancalaGame.class);

        assertThrows(ValidationException.class, () -> {
            validationService.validate(gameFromFrontEnd);
        }, "ValidationException was expected");
    }

    @Test
    public void testValidationPlayerOneSecondMove () throws Exception {
        MancalaGame playerOneFirstMoveGame = mapper.readValue(asInputStream (this.playerOneFirstMove), MancalaGame.class);
        doReturn(Optional.of(playerOneFirstMoveGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerOneSecondMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getMancalaBoard().toString())
                .isEqualTo("[0:0, 1:0, 2:8, 3:8, 4:8, 5:8, 6:2, 7:7, 8:7, 9:6, 10:6, 11:6, 12:6, 13:0]");
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_TWO);
        assertThat(validationResult.getWinner()).isNull();
    }

    @Test
    public void testValidationPlayerTwoFirstMove () throws Exception {
        MancalaGame playerOneSecondMoveGame = mapper.readValue(asInputStream (playerOneSecondMove), MancalaGame.class);
        playerOneSecondMoveGame.setActivePlayer(Player.PLAYER_TWO);
        doReturn(Optional.of(playerOneSecondMoveGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerTwoFirstMove), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getMancalaBoard().toString())
                .isEqualTo("[0:1, 1:1, 2:9, 3:9, 4:8, 5:8, 6:2, 7:7, 8:7, 9:6, 10:6, 11:0, 12:7, 13:1]");
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
        assertThat(validationResult.getWinner()).isNull();
    }

    @Test
    public void testValidationWinner () throws Exception {
        MancalaGame playerTwoWinPenultimateGame = mapper.readValue(asInputStream (playerTwoWinnerPenultimate), MancalaGame.class);
        doReturn(Optional.of(playerTwoWinPenultimateGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerTwoWinner), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getMancalaBoard().toString())
                .isEqualTo("[0:0, 1:0, 2:0, 3:0, 4:0, 5:0, 6:32, 7:0, 8:0, 9:0, 10:0, 11:0, 12:0, 13:40]");
        assertThat(validationResult.getWinner()).isEqualTo(Player.PLAYER_TWO);
        assertThat(validationResult.getGamePlayStatus()).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    public void testValidationWinnerInvalidStoneCount () throws Exception {
        MancalaGame playerTwoWinPenultimateGame = mapper.readValue(asInputStream (playerTwoWinnerPenultimate), MancalaGame.class);
        doReturn(Optional.of(playerTwoWinPenultimateGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerTwoWinnerInvalidStoneCount), MancalaGame.class);

        assertThrows(ValidationException.class, () -> {
            validationService.validate(gameFromFrontEnd);
        }, "ValidationException was expected");
    }

    @Test
    public void testValidationOppositeStoneCapture () throws Exception {
        MancalaGame playerTwoOppositeStoneCapturePenultimateGame = mapper.readValue(asInputStream (playerTwoStoneCapturePenultimate), MancalaGame.class);
        doReturn(Optional.of(playerTwoOppositeStoneCapturePenultimateGame)).when(mancalaRepository).findById(any(String.class));

        MancalaGame gameFromFrontEnd = mapper.readValue(asInputStream (playerTwoStoneCapture), MancalaGame.class);

        MancalaGame validationResult = validationService.validate(gameFromFrontEnd);
        assertThat(validationResult.getMancalaBoard().toString())
                .isEqualTo("[0:0, 1:0, 2:0, 3:0, 4:0, 5:2, 6:30, 7:0, 8:0, 9:0, 10:0, 11:0, 12:0, 13:40]");
        assertThat(validationResult.getActivePlayer()).isEqualTo(Player.PLAYER_ONE);
    }
}
