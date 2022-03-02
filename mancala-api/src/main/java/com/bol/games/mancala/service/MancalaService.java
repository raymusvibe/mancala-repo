package com.bol.games.mancala.service;

import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.exception.NotFoundException;
import com.bol.games.mancala.data.MancalaRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class MancalaService implements MancalaAPI {

    private MancalaRepository mancalaRepository;

    @Override
    public MancalaGame createGame() {
        MancalaGame mancala = new MancalaGame();
        mancalaRepository.save(mancala);
        return mancala;
    }

    @Override
    public MancalaGame connectToGame(String gameId) throws NotFoundException {
        Optional<MancalaGame> game = mancalaRepository.findById(gameId)
                .filter(it -> it.getGamePlayStatus().equals(GameStatus.NEW));
        if (!game.isPresent()) {
            throw new NotFoundException("Invalid GameId or this game is already in progress");
        }
        MancalaGame mancala = game.get();
        mancala.setGamePlayStatus(GameStatus.IN_PROGRESS);
        mancalaRepository.save(mancala);
        return mancala;
    }
}
