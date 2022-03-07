package com.bol.games.mancala.service;

import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.repository.abstractions.MancalaRepositoryAPI;
import com.bol.games.mancala.service.abstractions.MancalaAPI;
import com.bol.games.mancala.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service used to create new games and connect to already existing ones.
 */
@Service
@AllArgsConstructor
public class MancalaService implements MancalaAPI {

    @Autowired
    private MancalaRepositoryAPI mancalaRepository;

    /**
     * Service method called through constructor to create a new game.
     * A copy of the game is placed in both the games store and events store.
     * @return a new game instance.
     */
    @Override
    public final MancalaGame createGame() {
        MancalaGame mancala = new MancalaGame();
        mancala.initialiseBoard();
        mancalaRepository.insertGame(mancala);
        mancalaRepository.insertEvent(mancala);
        return mancala;
    }

    /**
     * Service method called through constructor when a second player wants to join an existing game
     * through a link send to them by a colleague.
     * @param gameId the id of the game they'll connect to
     * @return the new game instance they've connected to.
     */
    @Override
    public final MancalaGame connectToGame(String gameId) throws NotFoundException {
        MancalaGame game = mancalaRepository.findNewGame(gameId);
        if (game == null) {
            throw new NotFoundException("Invalid GameId or this game is already in progress");
        }
        game.setGamePlayStatus(GameStatus.IN_PROGRESS);
        mancalaRepository.saveGame(game);
        return game;
    }
}
