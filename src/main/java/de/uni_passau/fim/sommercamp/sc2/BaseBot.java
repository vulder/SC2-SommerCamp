package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.request.Request;
import com.github.ocraft.s2client.protocol.request.RequestGameInfo;
import com.github.ocraft.s2client.protocol.request.RequestStep;
import com.github.ocraft.s2client.protocol.response.*;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.syntax.action.raw.ActionRawUnitCommandBuilder;
import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ocraft.s2client.protocol.action.Action.action;
import static com.github.ocraft.s2client.protocol.action.ActionResult.SUCCESS;
import static com.github.ocraft.s2client.protocol.action.raw.ActionRawUnitCommand.unitCommand;
import static com.github.ocraft.s2client.protocol.data.Abilities.ATTACK;
import static com.github.ocraft.s2client.protocol.data.Abilities.MOVE;
import static com.github.ocraft.s2client.protocol.game.GameStatus.ENDED;
import static com.github.ocraft.s2client.protocol.game.Race.TERRAN;
import static com.github.ocraft.s2client.protocol.request.RequestAction.actions;
import static com.github.ocraft.s2client.protocol.request.RequestJoinGame.joinGame;
import static com.github.ocraft.s2client.protocol.request.RequestObservation.observation;

public abstract class BaseBot {

    private S2Client client;
    private GameInfo info;
    private GameObservation observation;
    private boolean lastActionSuccessful;

    public BaseBot(S2Client client) {
        this.client = client;
    }

    ///// GAME INTERFACE /////

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
            observation = new GameObservation(this, r.getObservation());
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


    ///// ACTIONS /////

    protected void moveUnits(Point2d target, BotUnit... units) {
        if (units.length != 0) {
            ActionRawUnitCommandBuilder action = unitCommand().forUnits(botUnits2Units(units)).useAbility(MOVE).target(target);
            client.request(actions().of(action().raw(action)));
        }
    }

    protected void attackTarget(BotUnit target, BotUnit... units) {
        if (units.length != 0) {
            client.request(actions().of(action().raw(unitCommand().forUnits(botUnits2Units(units))
                    .useAbility(ATTACK).target(target.getTag()))));
        }
    }

    protected void sendRawCommand(Request action) {
        client.request(action);
    }


    ///// GETTER /////

    protected List<BotUnit> getUnits() {
        return observation.units.stream().filter(BotUnit::isMine).collect(Collectors.toList());
    }

    protected List<BotUnit> getIdleUnits() {
        return getUnits().stream().filter(u -> u.getOrders().isEmpty()).collect(Collectors.toList());
    }

    protected List<BotUnit> getVisibleEnemies() {
        return observation.units.stream().filter(BotUnit::isEnemy).collect(Collectors.toList());
    }

    protected GameObservation getObservation() {
        return observation;
    }

    protected GameInfo getInfo() {
        return info;
    }

    protected boolean wasLastActionSuccessful() {
        return lastActionSuccessful;
    }


    ///// INTERNAL /////

    private Unit[] botUnits2Units(BotUnit[] units) {
        return Arrays.stream(units).map(BotUnit::getRawUnit).toArray(Unit[]::new);
    }

    private List<BotUnit> units2BotUnits(Stream<Unit> units) {
         return units.map(u -> new BotUnit(this, u)).collect(Collectors.toList());
    }
}
