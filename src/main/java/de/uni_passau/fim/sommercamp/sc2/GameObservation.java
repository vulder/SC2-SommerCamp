package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.protocol.observation.Observation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameObservation {
    private final BaseBot bot;
    private final Observation observation;

    public final List<BotUnit> units;
    public final long gameLoop;

    GameObservation(BaseBot bot, Observation observation) {
        this.bot = bot;
        this.observation = observation;

        this.units = observation.getRaw().get().getUnits().stream().map(u -> new BotUnit(bot, u)).collect(Collectors.toList());
        gameLoop = observation.getGameLoop();
    }

    public List<BotUnit> getDiedInLastStep() {
        return observation.getRaw().get().getEvent().map(e -> e.getDeadUnits().stream().map(u ->
                new BotUnit(bot, u)).collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    public Observation getRawObservation() {
        return observation;
    }
}
