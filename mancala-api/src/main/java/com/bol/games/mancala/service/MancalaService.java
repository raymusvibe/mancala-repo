package com.bol.games.mancala.service;

import com.bol.games.mancala.controller.dto.RestartRequest;
import com.bol.games.mancala.exception.IllegalRequestException;
import com.bol.games.mancala.exception.ValidationException;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.data.MancalaRepository;
import com.bol.games.mancala.exception.InvalidGameException;
import com.bol.games.mancala.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MancalaService implements MancalaAPI {

    @Autowired
    private MancalaRepository mancalaRepository;

    @Autowired
    private MancalaInputValidationService inputValidationService;

    @Override
    public MancalaGame createGame(Player playerOne) throws ValidationException {
        inputValidationService.validatePlayerName(playerOne.getPlayerName());
        MancalaGame mancala = new MancalaGame(playerOne);
        mancalaRepository.save(mancala);
        return mancala;
    }

    @Override
    public MancalaGame restartGame(RestartRequest request) throws ValidationException {
        inputValidationService.validateGameRestart(request);
        MancalaGame mancala = new MancalaGame(request.getPlayerOne());
        mancala.setPlayerTwo(request.getPlayerTwo());
        mancala.setGamePlayStatus(GameStatus.IN_PROGRESS);
        mancalaRepository.save(mancala);
        return mancala;
    }

    @Override
    public MancalaGame connectToGame(Player playerTwo, String gameId) throws ValidationException, InvalidGameException, IllegalRequestException {
        inputValidationService.validatePlayerName(playerTwo.getPlayerName());
        Optional<MancalaGame> game = mancalaRepository.findById(gameId)
                .filter(it -> it.getGamePlayStatus().equals(GameStatus.NEW));
        if (!game.isPresent()) {
            throw new InvalidGameException("You cannot connect to this game");
        }
        MancalaGame mancala = game.get();
        if (mancala.getPlayerTwo() != null) {
            throw new IllegalRequestException ("Another player is already connected to this game, please use a different GameId");
        }
        if (mancala.getPlayerOne().getPlayerName().equals(playerTwo.getPlayerName())) {
            throw new IllegalRequestException ("Please use a different player name to connected to this game");
        }
        mancala.setPlayerTwo(playerTwo);
        mancala.setGamePlayStatus(GameStatus.IN_PROGRESS);
        mancalaRepository.save(mancala);
        return mancala;
    }

    @Override
    public MancalaGame connectToRandomGame(Player playerTwo) throws ValidationException, NotFoundException {
        inputValidationService.validatePlayerName(playerTwo.getPlayerName());
        MancalaGame mancala = mancalaRepository.findAll().stream()
                .filter(it -> it.getGamePlayStatus().equals(GameStatus.NEW)
                        && it.getPlayerTwo() == null
                        && !it.getPlayerOne().getPlayerName().equalsIgnoreCase(playerTwo.getPlayerName()))
                .findFirst().orElseThrow(() -> new NotFoundException("Open game not found or try with a different player name"));
        mancala.setPlayerTwo(playerTwo);
        mancala.setGamePlayStatus(GameStatus.IN_PROGRESS);
        mancalaRepository.save(mancala);
        return mancala;
    }
}
