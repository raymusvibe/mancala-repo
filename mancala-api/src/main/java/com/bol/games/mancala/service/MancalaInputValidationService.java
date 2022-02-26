package com.bol.games.mancala.service;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.RestartRequest;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.service.abstractions.MancalaInputValidationAPI;
import org.springframework.stereotype.Service;

@Service
public class MancalaInputValidationService implements MancalaInputValidationAPI {
    @Override
    public void validatePlayerName(String input) throws ValidationException {
        if (input == null || input.isBlank() || input.length() > MancalaConstants.PLAYER_NAME_INPUT_LENGTH_MAX)
            throw new ValidationException("Player name cannot be blank or have a length more than "
                    + MancalaConstants.PLAYER_NAME_INPUT_LENGTH_MAX);
    }

    @Override
    public void validateGameRestart(RestartRequest request) throws ValidationException {
        validatePlayerName(request.getPlayerOne().getPlayerName());
        validatePlayerName(request.getPlayerTwo().getPlayerName());
        if (request.getPlayerOne().getPlayerName().equals(request.getPlayerTwo().getPlayerName()))
            throw new ValidationException("Player names must be different");
    }
}
