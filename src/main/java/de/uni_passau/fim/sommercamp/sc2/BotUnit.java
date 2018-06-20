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

@SuppressWarnings({"WeakerAccess", "unused"})
public class BotUnit {

    private final BaseBot bot;
    private final Tag tag;

    BotUnit(BaseBot bot, Tag tag) {
        this.bot = bot;
        this.tag = tag;
    }

    BotUnit(BaseBot bot, Unit unit) {
        this(bot, unit.getTag());
    }


    ///// GETTER /////

    public Unit getRawUnit() {
        return getByTag(tag);
    }

    public List<UnitOrder> getOrders() {
        return getByTag(tag).getOrders();
    }

    public boolean isAliveOrVisible() {
        return findByTag(tag).isPresent();
    }

    public boolean isMine() {
        return getByTag(tag).getAlliance() == SELF;
    }

    public boolean isEnemy() {
        return getByTag(tag).getAlliance() == ENEMY;
    }

    public UnitType getType() {
        return getByTag(tag).getType();
    }

    public Point2d getPosition() {
        Point position = getByTag(tag).getPosition();
        return Point2d.of(position.getX(), position.getY());
    }

    public float getFacing() {
        return getByTag(tag).getFacing();
    }

    public boolean isSelected() {
        return getByTag(tag).getSelected().orElse(false);
    }

    public float getHealth() {
        return getByTag(tag).getHealth().orElse(0f);
    }

    public float getMaxHealth() {
        return getByTag(tag).getHealthMax().orElse(0f);
    }

    public float getWeaponCooldown() {
        return getByTag(tag).getWeaponCooldown().orElse(Float.MAX_VALUE);
    }

    public Optional<BotUnit> getEngagedTarget() {
        return getByTag(tag).getEngagedTargetTag().map(t -> new BotUnit(bot, t));
    }


    ///// ACTIONS /////

    public void move(Point2d target) {
        findByTag(tag).ifPresent(u -> bot.moveUnits(target, this));
    }

    public void attack(BotUnit target) {
        findByTag(tag).ifPresent(u -> bot.attackTarget(target, this));
    }


    ///// INTERNAL /////

    private Unit getByTag(Tag tag) {
        return findByTag(tag).orElseThrow(() -> new UnitNotFoundException("The specified unit is not visible or not alive."));
    }

    private Optional<Unit> findByTag(Tag tag) {
        return bot.getObservation().units.stream().filter(u -> u.getTag().equals(tag)).findFirst().map(BotUnit::getRawUnit);
    }

    Tag getTag() {
        return tag;
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
        return String.format("Unit[id=%s(%s), position=%s, health=%s]", tag, getRawUnit().getAlliance(), getPosition(), getHealth());
    }
}
