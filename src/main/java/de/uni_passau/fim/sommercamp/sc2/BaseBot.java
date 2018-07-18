package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.request.Request;
import com.github.ocraft.s2client.protocol.request.RequestData;
import com.github.ocraft.s2client.protocol.request.RequestGameInfo;
import com.github.ocraft.s2client.protocol.request.RequestStep;
import com.github.ocraft.s2client.protocol.response.*;
import com.github.ocraft.s2client.protocol.syntax.action.raw.ActionRawUnitCommandBuilder;
import com.github.ocraft.s2client.protocol.unit.Unit;
import de.uni_passau.fim.sommercamp.sc2.util.Vec2;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import static com.github.ocraft.s2client.protocol.request.RequestData.Type.UNITS;
import static com.github.ocraft.s2client.protocol.request.RequestJoinGame.joinGame;
import static com.github.ocraft.s2client.protocol.request.RequestObservation.observation;

/**
 * A BaseBot is the base class for all Starcraft II bots with the modified ocraft s2client API.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseBot {

    public static long FRAME_RATE = 20;

    private final static Abilities.Other HEAL = Abilities.Other.of(2750);

    private S2Client client;
    private GameInfo info;
    private GameObservation observation;
    private final AtomicBoolean lastActionSuccessful = new AtomicBoolean(true);
    private final long frameDelta = 1000 / FRAME_RATE;
    private long lastFrame = 0;
    private boolean init = false;

    /**
     * Creates a new BaseBot for the given S2Client.
     *
     * @param client the client connection the this bot to the Starcraft II instance
     */
    public BaseBot(S2Client client) {
        this.client = client;
    }

    ///// GAME INTERFACE /////

    /**
     * This method is called by the library to handle Responses sent by the game.
     *
     * @param response the Response to handle
     */
    public final void handle(Response response) {
        response.as(ResponseCreateGame.class).ifPresent(r -> client.request(joinGame().as(TERRAN)));
        response.as(ResponseJoinGame.class).ifPresent(r -> client.request(RequestGameInfo.gameInfo()));
        response.as(ResponseStep.class).ifPresent(r -> client.request(observation()));
        response.as(ResponseAction.class).ifPresent(r -> {
            synchronized (lastActionSuccessful) {
                lastActionSuccessful.set(r.getResults().stream().allMatch(ar -> ar == SUCCESS));
            }
        });
        response.as(ResponseGameInfo.class).ifPresent(r -> {
            info = new GameInfo(r.getPlayersInfo(), r.getMapName(), r.getStartRaw().orElse(null));
            client.request(observation().disableFog());
        });
        response.as(ResponseData.class).ifPresent(r -> {
            BotUnit.fillData(r.getUnitTypes());
            client.request(observation());
        });

        // main game loop
        response.as(ResponseObservation.class).ifPresent(r -> {
            observation = new GameObservation(this, r.getObservation());
            if (!init) {
                BotUnit.updateCache(observation.getUnits());
                init = true;
                client.request(RequestData.data().of(UNITS));
                return;
            }

            if (r.getStatus() != ENDED) {
                onStep();
                ensureFps();
                client.request(RequestStep.nextStep());
//            TODO check how to allow manual replays but exit the game gracefully
//            } else {
//                // Disabled to allow manual replays
//                client.request(leaveGame());
            }
        });
    }

    /**
     * This method is called on each step/once each game loop. When implementing a bot, place all logic and action here.
     */
    protected abstract void onStep();


    ///// ACTIONS /////

    /**
     * Sends a MOVE request for the given BotUnits to the given map location.
     * <p>
     * If the position is not valid, the request will fail to apply.
     * If no (alive) units are supplied, no request will be sent.
     *
     * @param target a valid Vec2 describing the target location on the map
     * @param units  the BotUnits this action is sent to
     * @see GameInfo#mapData GameInfo.mapData, for information on the map
     * @see #wasLastActionSuccessful() wasLastActionSuccessful(), to check if the request was successful
     */
    protected void moveUnits(Vec2 target, BotUnit... units) {
        if (units.length != 0 && target.isValidPoint2d()) {
            ActionRawUnitCommandBuilder action = unitCommand().forUnits(botUnits2Units(Arrays.stream(units))).useAbility(MOVE).target(target.getPoint2d().get());
            client.request(actions().of(action().raw(action)));
        }
    }

    /**
     * Sends a MOVE request for the given BotUnits to the given map location.
     * <p>
     * If the position is not valid, the request will fail to apply.
     * If no (alive) units are supplied, no request will be sent.
     *
     * @param target a valid Vec2 describing the target location on the map
     * @param units  the BotUnits this action is sent to
     * @see GameInfo#mapData GameInfo.mapData, for information on the map
     * @see #wasLastActionSuccessful() wasLastActionSuccessful(), to check if the request was successful
     */
    protected void moveUnits(Vec2 target, List<BotUnit> units) {
        if (!units.isEmpty() && target.isValidPoint2d()) {
            ActionRawUnitCommandBuilder action = unitCommand().forUnits(botUnits2Units(units.stream())).useAbility(MOVE).target(target.getPoint2d().get());
            client.request(actions().of(action().raw(action)));
        }
    }

    /**
     * Sends ATTACK requests for the given BotUnits to attack the given BotUnit target.
     * <p>
     * If the target is not alive or visible, the request will fail to apply.
     * If no (alive) units are supplied, no request will be sent.
     *
     * @param target the target BotUnit
     * @param units  the BotUnits this action is sent to
     * @see #wasLastActionSuccessful() wasLastActionSuccessful(), to check if the request was successful
     */
    protected void attackTarget(BotUnit target, BotUnit... units) {
        if (units.length != 0) {
            client.request(actions().of(action().raw(unitCommand().forUnits(botUnits2Units(Arrays.stream(units)))
                    .useAbility(ATTACK).target(target.getTag()))));
        }
    }

    /**
     * Sends ATTACK requests for the given BotUnits to attack the given BotUnit target.
     * <p>
     * If the target is not alive or visible, the request will fail to apply.
     * If no (alive) units are supplied, no request will be sent.
     *
     * @param target the target BotUnit
     * @param units  the BotUnits this action is sent to
     * @see #wasLastActionSuccessful() wasLastActionSuccessful(), to check if the request was successful
     */
    protected void attackTarget(BotUnit target, List<BotUnit> units) {
        if (!units.isEmpty()) {
            client.request(actions().of(action().raw(unitCommand().forUnits(botUnits2Units(units.stream()))
                    .useAbility(ATTACK).target(target.getTag()))));
        }
    }

    /**
     * Sends HEAL requests for the given BotUnits to heal the given BotUnit target.
     * <p>
     * If the target is not alive or visible, the request will fail to apply.
     * If no (alive) units are supplied, no request will be sent.
     *
     * @param target the target BotUnit
     * @param units  the BotUnits this action is sent to
     * @see #wasLastActionSuccessful() wasLastActionSuccessful(), to check if the request was successful
     */
    protected void healTarget(BotUnit target, BotUnit... units) {
        if (units.length != 0) {
            client.request(actions().of(action().raw(unitCommand().forUnits(botUnits2Units(Arrays.stream(units)))
                    .useAbility(HEAL).target(target.getTag()))));
        }
    }

    /**
     * Sends HEAL requests for the given BotUnits to heal the given BotUnit target.
     * <p>
     * If the target is not alive or visible, the request will fail to apply.
     * If no (alive) units are supplied, no request will be sent.
     *
     * @param target the target BotUnit
     * @param units  the BotUnits this action is sent to
     * @see #wasLastActionSuccessful() wasLastActionSuccessful(), to check if the request was successful
     */
    protected void healTarget(BotUnit target, List<BotUnit> units) {
        if (!units.isEmpty()) {
            client.request(actions().of(action().raw(unitCommand().forUnits(botUnits2Units(units.stream()))
                    .useAbility(HEAL).target(target.getTag()))));
        }
    }

    /**
     * Send a raw request to the game.
     *
     * @param action the request to send
     */
    protected void sendRawCommand(Request action) {
        client.request(action);
    }


    ///// GETTER /////

    /**
     * Gets a list of all BotUnits belonging to this bot.
     *
     * @return a list of BotUnits of this bot.
     */
    protected List<BotUnit> getUnits() {
        return observation.getUnits().stream().filter(BotUnit::isMine).collect(Collectors.toList());
    }

    /**
     * Gets a list of all BotUnits belonging to this bot, that currently have no orders.
     *
     * @return a list of BotUnits
     */
    protected List<BotUnit> getIdleUnits() {
        return getUnits().stream().filter(u -> u.getOrders().isEmpty()).collect(Collectors.toList());
    }

    /**
     * Gets a list of all visible BotUnits that are classified as enemies.
     *
     * @return a list of BotUnits currently visible by the bot, classified a ENEMY
     */
    protected List<BotUnit> getVisibleEnemies() {
        return observation.getUnits().stream().filter(BotUnit::isEnemy).collect(Collectors.toList());
    }

    /**
     * Gets a GameObservation containing information about the current game state.
     *
     * @return a GameObservation describing the game state at the current game loop
     */
    protected GameObservation getObservation() {
        return observation;
    }

    /**
     * Gets general information about the current game.
     *
     * @return static information about the current game
     */
    protected GameInfo getInfo() {
        return info;
    }

    /**
     * Checks if the last sent action was successful.
     * Note, that generally only the last action sent in each step is recorded.
     *
     * @return {@code true} if the last action sent to the game was a SUCCESS, {@code false} otherwise.
     */
    protected boolean wasLastActionSuccessful() {
        synchronized (lastActionSuccessful) {
            return lastActionSuccessful.get();
        }
    }


    ///// INTERNAL /////

    /**
     * Converts an array of BotUnits to an array of Units.
     *
     * @param units the BotUnits
     * @return the underlying Units
     * @see #units2BotUnits(Stream)
     */
    private Unit[] botUnits2Units(Stream<BotUnit> units) {
        return units.map(BotUnit::getUnit).filter(Optional::isPresent).map(Optional::get).toArray(Unit[]::new);
    }

    /**
     * Converts a stream of Units to a list of BotUnits
     *
     * @param units the Units
     * @return the BotUnits wrapping the given units
     * @see #botUnits2Units(Stream)
     */
    private List<BotUnit> units2BotUnits(Stream<Unit> units) {
        return units.map(u -> new BotUnit(this, u)).collect(Collectors.toList());
    }

    /**
     * Sleeps some time to enforce target-fps (= simulation steps per second)
     */
    private void ensureFps() {
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - lastFrame;
        if (delta < frameDelta) {
            try {
                TimeUnit.MILLISECONDS.sleep(frameDelta - delta);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        lastFrame = currentTime;
    }
}
