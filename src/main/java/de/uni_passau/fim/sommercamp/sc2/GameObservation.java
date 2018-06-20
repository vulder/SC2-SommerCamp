package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.protocol.observation.Observation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GameObservation is a wrapper for game state Observations.
 */
public class GameObservation {
    private final BaseBot bot;
    private final Observation observation;

    /**
     * A list of all currently visible units.
     */
    public final List<BotUnit> units;

    /**
     * The game loop count.
     */
    public final long gameLoop;

    /**
     * Creates a new GameObservation with the current game state.
     *
     * @param bot         the bot to interact with the game
     * @param observation the Observation about the current game state
     */
    GameObservation(BaseBot bot, Observation observation) {
        this.bot = bot;
        this.observation = observation;

        this.units = observation.getRaw().get().getUnits().stream().map(u -> new BotUnit(bot, u)).collect(Collectors.toList());
        gameLoop = observation.getGameLoop();
    }

    /**
     * Gets a list of all units that died in the last game loop.
     *
     * @return a list of all units that died since the last step
     */
    public List<BotUnit> getDiedInLastStep() {
        return observation.getRaw().get().getEvent().map(e -> e.getDeadUnits().stream().map(u ->
                new BotUnit(bot, u)).collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    /**
     * Gets the raw game state Observation for additional information.
     *
     * @return the Observation about the current game state
     */
    public Observation getRawObservation() {
        return observation;
    }
}
