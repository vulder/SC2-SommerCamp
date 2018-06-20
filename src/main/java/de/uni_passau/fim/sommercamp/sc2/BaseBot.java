package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.observation.Observation;
import com.github.ocraft.s2client.protocol.request.Request;
import com.github.ocraft.s2client.protocol.request.RequestGameInfo;
import com.github.ocraft.s2client.protocol.request.RequestStep;
import com.github.ocraft.s2client.protocol.response.*;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.syntax.action.raw.ActionRawUnitCommandBuilder;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.Arrays;
import java.util.Optional;

import static com.github.ocraft.s2client.protocol.action.Action.action;
import static com.github.ocraft.s2client.protocol.action.ActionResult.SUCCESS;
import static com.github.ocraft.s2client.protocol.action.raw.ActionRawUnitCommand.unitCommand;
import static com.github.ocraft.s2client.protocol.data.Abilities.*;
import static com.github.ocraft.s2client.protocol.game.GameStatus.ENDED;
import static com.github.ocraft.s2client.protocol.game.Race.TERRAN;
import static com.github.ocraft.s2client.protocol.request.RequestAction.actions;
import static com.github.ocraft.s2client.protocol.request.RequestJoinGame.joinGame;
import static com.github.ocraft.s2client.protocol.request.RequestObservation.observation;
import static com.github.ocraft.s2client.protocol.unit.Alliance.ENEMY;
import static com.github.ocraft.s2client.protocol.unit.Alliance.SELF;

public abstract class BaseBot {

    private S2Client client;
    private GameInfo info;
    private Observation observation;
    private boolean lastActionSuccessful;

    public BaseBot(S2Client client) {
        this.client = client;
    }

    public final void handle(Response response) {
        response.as(ResponseCreateGame.class).ifPresent(r -> client.request(joinGame().as(TERRAN)));
        response.as(ResponseJoinGame.class).ifPresent(r -> client.request(RequestGameInfo.gameInfo()));
        response.as(ResponseStep.class).ifPresent(r -> client.request(observation()));
        response.as(ResponseAction.class).ifPresent(r -> lastActionSuccessful = r.getResults().stream().allMatch(ar -> ar == SUCCESS));
        response.as(ResponseGameInfo.class).ifPresent(r -> {
            info = new GameInfo(r.getPlayersInfo(), r.getMapName(), r.getStartRaw().orElse(null));
            client.request(observation());
        });

        // main game loop
        response.as(ResponseObservation.class).ifPresent(r -> {
            observation = r.getObservation();
            if (r.getStatus() != ENDED) {
                onStep();
                client.request(RequestStep.nextStep());
//            } else {
//                // Disabled to allow manual replays
//                client.request(leaveGame());
            }
        });
    }

    protected abstract void onStep();

    protected void moveUnits(Point2d target, BotUnit... units) {
        if (units.length != 0) {
            ActionRawUnitCommandBuilder action = unitCommand().forUnits(botUnits2Units(units)).useAbility(MOVE).target(target);
            client.request(actions().of(action().raw(action)));
        }
    }

    protected void attackTarget(BotUnit target, BotUnit... units) {
        if (units.length != 0) {
            client.request(actions().of(action().raw(unitCommand().forUnits(botUnits2Units(units)).useAbility(ATTACK).target(target.getTag()))));
        }
    }

    protected void sendRawCommand(Request action) {
        client.request(action);
    }

    protected Unit[] getUnits() {
        return observation.getRaw().get().getUnits().stream().filter(u -> u.getAlliance() == SELF).toArray(Unit[]::new);
    }

    protected Unit[] getIdleUnits() {
        return observation.getRaw().get().getUnits().stream().filter(u -> u.getAlliance() == SELF)
                .filter(u -> u.getOrders().isEmpty()).toArray(Unit[]::new);
    }

    protected Unit[] getVisibleEnemies() {
        return observation.getRaw().get().getUnits().stream().filter(u -> u.getAlliance() == ENEMY).toArray(Unit[]::new);
    }

    protected Optional<Unit> findByTag(Tag tag) {
        return getObservation().getRaw().get().getUnits().stream().filter(u -> u.getTag().equals(tag)).findFirst();
    }

    protected Observation getObservation() {
        return observation;
    }

    protected GameInfo getInfo() {
        return info;
    }

    public boolean wasLastActionSuccessful() {
        return lastActionSuccessful;
    }

    private Unit[] botUnits2Units(BotUnit[] units) {
        return Arrays.stream(units).map(BotUnit::getUnit).toArray(Unit[]::new);
    }
}
