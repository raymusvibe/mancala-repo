package com.bol.games.mancala.service.gameplay;

import com.bol.games.mancala.constants.MancalaConstants;
import com.bol.games.mancala.controller.dto.GamePlay;
import com.bol.games.mancala.model.GameStatus;
import com.bol.games.mancala.repository.MancalaRepository;
import com.bol.games.mancala.model.MancalaGame;
import com.bol.games.mancala.service.gameplay.abstractions.GameRule;
import com.bol.games.mancala.util.DummyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static com.bol.games.mancala.util.ResourceReader.resourceAsInputStream;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StoneSowingRuleUnitTests {
    @Mock
    private MancalaRepository mancalaRepository;

    private static final StoneSowingRule stoneSowingRule = new StoneSowingRule();
    private static final GameRule dummyRule = new DummyRule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Resource repoStateBeforePlayerTwoOppositeStoneCapture = new ClassPathResource("gameStateBeforePlayerTwoOppositeStoneCapture.json");
    private final Resource repoStateBeforePlayerOneFullCircleSowing = new ClassPathResource("gameStateBeforePlayerOneFullCircleSowing.json");
    private final Resource repoStateBeforePlayerOnePastFullCircleSowing = new ClassPathResource("gameStateBeforePlayerOnePastFullCircleSowing.json");

    @BeforeAll
    public static void setUp () {
        stoneSowingRule.setSuccessor(dummyRule);
    }

    @Test
    void StoneSowingRule_WhenSowingIntoEmptyPot_OppositePotIsCaptured () throws Exception {
        MancalaGame game = mapper.readValue(resourceAsInputStream(repoStateBeforePlayerTwoOppositeStoneCapture), MancalaGame.class);

        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.IN_PROGRESS, 11);
        stoneSowingRule.executeRule(gamePlay,
                                    game,
                                    mancalaRepository);
        //Pot 11 has one stone, sows that one stone into empty port 12, and captures opposite pot 0
        assertThat(game.getStoneContainer(0).getStones()).isEqualTo(MancalaConstants.EMPTY_STONE_COUNT);
    }

    @Test
    void StoneSowingRule_WhenSowingGoesFullCircle_OppositePotIsCaptured () throws Exception {
        MancalaGame game = mapper.readValue(resourceAsInputStream(repoStateBeforePlayerOneFullCircleSowing), MancalaGame.class);

        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.IN_PROGRESS, 0);
        stoneSowingRule.executeRule(gamePlay,
                                    game,
                                    mancalaRepository);
        //Steal from pot 12 after going all way round
        assertThat(game.getStoneContainer(0).getStones()).isEqualTo(MancalaConstants.EMPTY_STONE_COUNT);
        assertThat(game.getStoneContainer(12).getStones()).isEqualTo(MancalaConstants.EMPTY_STONE_COUNT);
    }

    @Test
    void StoneSowingRule_WhenSowingGoesBeyondFullCircle_OppositePotIsNotCaptured () throws Exception {
        MancalaGame game = mapper.readValue(resourceAsInputStream(repoStateBeforePlayerOnePastFullCircleSowing), MancalaGame.class);

        GamePlay gamePlay = new GamePlay(game.getGameId(), GameStatus.IN_PROGRESS, 0);
        stoneSowingRule.executeRule(gamePlay,
                game,
                mancalaRepository);
        //Sow into selected index and go beyond
        assertThat(game.getStoneContainer(0).getStones()).isEqualTo(MancalaConstants.LAST_STONE_COUNT);
        assertThat(game.getStoneContainer(1).getStones()).isEqualTo(MancalaConstants.LAST_STONE_COUNT + 1);
        assertThat(game.getStoneContainer(12).getStones()).isEqualTo(MancalaConstants.LAST_STONE_COUNT);
    }
}