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
 * Service used to create new games and connect to already existing games.
 */
@Service
@AllArgsConstructor
public class MancalaService implements MancalaAPI {

    @Autowired
    private MancalaRepositoryAPI mancalaRepository;

    /**
     * Service method called through controller to create a new game.
     * @return MancalaGame, a new game instance.
     */
    @Override
    public final MancalaGame createGame() {
        MancalaGame mancala = new MancalaGame();
        mancala.initialiseBoardToStartNewGame();
        mancalaRepository.insertGame(mancala);
        return mancala;
    }

    /**
     * Service method called through controller when a second player
     * wants to join an existing game through a link send to them by a
     * friend. A game ID can only be used to connect to a game only
     * once and when the game is in a NEW state.
     * @param gameId the id of the game they'll connect to
     * @return MancalaGame, the new game instance client has connected to.
     */
    @Override
    public final MancalaGame connectToGame(String gameId) throws NotFoundException {
        MancalaGame game = mancalaRepository.findNewGame(gameId);
        if (game == null) {
            throw new NotFoundException("Invalid game ID or this game ID has already been used");
        }
        game.setGamePlayStatus(GameStatus.IN_PROGRESS);
        mancalaRepository.saveGame(game);
        return game;
    }
}
