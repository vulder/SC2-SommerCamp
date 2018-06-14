package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.observation.Observation;
import com.github.ocraft.s2client.protocol.request.RequestGameInfo;
import com.github.ocraft.s2client.protocol.request.RequestStep;
import com.github.ocraft.s2client.protocol.response.*;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;

import static com.github.ocraft.s2client.protocol.action.Action.action;
import static com.github.ocraft.s2client.protocol.action.raw.ActionRawUnitCommand.unitCommand;
import static com.github.ocraft.s2client.protocol.data.Abilities.*;
import static com.github.ocraft.s2client.protocol.request.RequestAction.actions;
import static com.github.ocraft.s2client.protocol.request.RequestJoinGame.joinGame;
import static com.github.ocraft.s2client.protocol.request.RequestObservation.observation;

public abstract class BaseBot {

    private final Race race;
    private S2Client client;
    private GameInfo info;
    private Observation observation;

    public BaseBot(S2Client client, Race race) {
        this.race = race;
        this.client = client;
    }

    public final void handle(Response response) {
        response.as(ResponseCreateGame.class).ifPresent(r -> client.request(joinGame().as(race)));
        response.as(ResponseJoinGame.class).ifPresent(r -> client.request(RequestGameInfo.gameInfo()));
        response.as(ResponseGameInfo.class).ifPresent(r -> {
            info = new GameInfo(r.getPlayersInfo(), r.getMapName(), r.getStartRaw().orElse(null));
            client.request(observation());
        });

        response.as(ResponseStep.class).ifPresent(r  -> client.request(observation()));

        // main game loop
        response.as(ResponseObservation.class).ifPresent(r -> {
            observation = r.getObservation();
            onStep();
            client.request(RequestStep.nextStep());
        });

        System.err.println(response.getType());
    }

    protected abstract void onStep();

    protected void moveUnits(Unit[] units, Point2d target) {
        client.request(actions().of(action().raw(unitCommand().forUnits(units).useAbility(MOVE).target(target))));
    }

    protected void attackTarget(Unit[] units, Tag target) {
        client.request(actions().of(action().raw(unitCommand().forUnits(units).useAbility(ATTACK).target(target))));
    }

    protected void stopUnits(Unit[] units) {
        client.request(actions().of(action().raw(unitCommand().forUnits(units).useAbility(STOP).build())));
    }

    protected Unit[] getIdleUnits() {
        return observation.getRaw().get().getUnits().stream().filter(u -> u.getOrders().isEmpty()).toArray(Unit[]::new);
    }

    public Race getRace() {
        return race;
    }

    protected Observation getObservation() {
        return observation;
    }
}
