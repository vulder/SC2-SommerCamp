package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.ocraft.s2client.protocol.unit.Alliance.ENEMY;
import static com.github.ocraft.s2client.protocol.unit.Alliance.SELF;

/**
 * A BotUnit represents one unit in the game. All units, including enemies and any other units are wrapped in this
 * class, but actions can only be executed on units belonging to this bot.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BotUnit {

    private final BaseBot bot;
    private final Tag tag;

    /**
     * Creates a new BotUnit wrapping the underlying Unit with the given Tag.
     *
     * @param bot the bot for interacting with the unit.
     * @param tag the Tag of the underlying Unit
     */
    BotUnit(BaseBot bot, Tag tag) {
        this.bot = bot;
        this.tag = tag;
    }

    /**
     * Creates a new BotUnit wrapping the given underlying Unit.
     *
     * @param bot  the bot for interacting with the unit.
     * @param unit the underlying Unit
     */
    BotUnit(BaseBot bot, Unit unit) {
        this(bot, unit.getTag());
    }


    ///// GETTER /////

    /**
     * Gets the underlying Unit for direct access of additional data.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return the underlying Unit wrapped by this BotUnit
     */
    public Unit getRawUnit() {
        return getByTag(tag);
    }

    /**
     * Gets a list of all queued Orders issued to this unit.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return a list of UnitOrders
     */
    public List<UnitOrder> getOrders() {
        return getByTag(tag).getOrders();
    }

    /**
     * Checks if the unit represented by this, is still alive, and if it does not belong to this bot, if it is currently
     * visible by any unit of this bot.
     *
     * @return {@code true} if the unit is alive and visible, and therefore, if information about it can be obtained,
     * {@code false} otherwise
     * @see GameObservation#getDiedInLastStep() GameObservation.getDiedInLastStep() to check if a Unit actually died in
     * the last game loop
     */
    public boolean isAliveAndVisible() {
        return findByTag(tag).isPresent();
    }

    /**
     * Checks if this unit belongs to this bot.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return {@code true} if the unit belongs to this bot, {@code false} otherwise
     */
    public boolean isMine() {
        return getByTag(tag).getAlliance() == SELF;
    }

    /**
     * Checks if this unit does not belong to this bot and is marked as ENEMY.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return {@code true} if the unit is an enemy, {@code false} otherwise
     */
    public boolean isEnemy() {
        return getByTag(tag).getAlliance() == ENEMY;
    }

    /**
     * Gets the type of this unit.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return the UnitType
     */
    public UnitType getType() {
        return getByTag(tag).getType();
    }

    /**
     * Gets the position on the map grid, at which this unit is currently.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return a Point2d representing the position
     * @see GameInfo#mapData GameInfo.mapData, for the map grid
     */
    public Point2d getPosition() {
        Point position = getByTag(tag).getPosition();
        return Point2d.of(position.getX(), position.getY());
    }

    /**
     * Get the angle (in radians) the unit faces.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return the angle in radians
     */
    public float getFacing() {
        return getByTag(tag).getFacing();
    }

    /**
     * Checks if the unit is selected in the UI.
     *
     * @return {@code true}, if this unit {@link #isAliveAndVisible() is alive and visible} and was selected
     */
    public boolean isSelected() {
        return findByTag(tag).flatMap(Unit::getSelected).orElse(false);
    }

    /**
     * Gets the health of the unit.
     *
     * @return the health of the unit
     * @see #getMaxHealth() getMaxHealth(), for the maximal health of this unit
     */
    public float getHealth() {
        return findByTag(tag).flatMap(Unit::getHealth).orElse(0f);
    }

    /**
     * Gets health of this unit at the beginning of the game.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return the maximal health of this unit or {@code -1} if it has no available health data
     */
    public float getMaxHealth() {
        return getByTag(tag).getHealthMax().orElse(-1f);
    }

    /**
     * Gets the time, until the weapon can be used again.
     * <p>
     * Note, make sure the unit {@link #isAliveAndVisible() is alive and visible}, otherwise a
     * {@link UnitNotFoundException} will be thrown.
     *
     * @return the time in in-game seconds/game loops until the weapon can be used again or {@link Float#MAX_VALUE} if
     * this unit has no weapon cooldown
     */
    public float getWeaponCooldown() {
        return getByTag(tag).getWeaponCooldown().orElse(Float.MAX_VALUE);
    }

    /**
     * Checks if this unit has engaged on a target.
     *
     * @return {@code true}, if the unit {@link #isAliveAndVisible() is alive and visible} and is engaged on a target,
     * {@code false} otherwise
     */
    public Optional<BotUnit> getEngagedTarget() {
        return findByTag(tag).flatMap(u -> u.getEngagedTargetTag().map(t -> new BotUnit(bot, t)));
    }


    ///// ACTIONS /////

    /**
     * Sends a MOVE request, to move this unit to the specified position.
     * <p>
     * Note, will only be successful if this unit is {@link #isMine() controlled by this bot}.
     *
     * @param target a valid position on the map
     * @see GameInfo#mapData GameInfo.mapData, for information on the map
     * @see BaseBot#moveUnits(Point2d, BotUnit...)
     */
    public void move(Point2d target) {
        findByTag(tag).ifPresent(u -> bot.moveUnits(target, this));
    }

    /**
     * Sends a ATTACK request, to attack the given (enemy) unit by this unit.
     * <p>
     * Note, will only be successful if this unit is {@link #isMine() controlled by this bot}.
     *
     * @param target the target enemy unit
     * @see BaseBot#attackTarget(BotUnit, BotUnit...)
     */
    public void attack(BotUnit target) {
        findByTag(tag).ifPresent(u -> bot.attackTarget(target, this));
    }


    ///// INTERNAL /////

    /**
     * Finds the Unit with the given tag in the current game state.
     *
     * @param tag the tag to find
     * @return the Unit with this tag
     * @throws UnitNotFoundException if no unit with this tag can be found in the current game state
     */
    private Unit getByTag(Tag tag) {
        return findByTag(tag).orElseThrow(() -> new UnitNotFoundException("The specified unit is not visible or not alive."));
    }

    /**
     * Tries to find a Unit with the given tag in the current game state
     *
     * @param tag the tag to find
     * @return optionally a Unit with this tag, of an empty Optional, if there is none
     */
    private Optional<Unit> findByTag(Tag tag) {
        return bot.getObservation().getRawObservation().getRaw().get().getUnits().stream()
                .filter(u -> u.getTag().equals(tag)).findFirst();
    }

    /**
     * Gets the Tag to identify this unit.
     *
     * @return the Tag
     */
    Tag getTag() {
        return tag;
    }

    /**
     * Gets the underlying Unit
     *
     * @return optionally the underlying unit, if it is visible and alive, otherwise an empty Optional
     * @see #getRawUnit()
     * @see #isAliveAndVisible()
     */
    Optional<Unit> getUnit() {
        return findByTag(tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotUnit)) return false;
        BotUnit botUnit = (BotUnit) o;
        return Objects.equals(tag, botUnit.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public String toString() {
        if (isAliveAndVisible()) {
            return String.format("Unit[id=%s (%s), position=%s, health=%s]", tag.getValue(),
                    getRawUnit().getAlliance(), getPosition(), getHealth());
        } else {
            return String.format("Unit[id=%s]", tag.getValue());
        }
    }
}
