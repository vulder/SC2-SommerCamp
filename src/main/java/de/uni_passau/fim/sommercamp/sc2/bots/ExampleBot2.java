package de.uni_passau.fim.sommercamp.sc2.bots;

import com.github.ocraft.s2client.api.S2Client;
import de.uni_passau.fim.sommercamp.sc2.BaseBot;
import de.uni_passau.fim.sommercamp.sc2.BotUnit;
import de.uni_passau.fim.sommercamp.sc2.util.Vec2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ExampleBot2 extends BaseBot {

    private boolean searching = false;
    private boolean attacking = false;
    private boolean needCooling = false;

    private BotUnit target;
    private int leg = 0;

    public ExampleBot2(S2Client client) {
        super(client);
    }

    @Override
    protected void onStep() {
        List<BotUnit> aliveUnits = getUnits().stream()
                .filter(BotUnit::isAliveAndVisible)
                .collect(Collectors.toList());
        if (aliveUnits.isEmpty()) {
            return;
        }
        BotUnit leader = aliveUnits.get(0);

        if (!searching && !attacking) {
            searching = true;

            Vec2 base = leader.getPosition();
            if (base.getX() > getInfo().getMapData().getMapSize().getX() / 2) {
                if (base.getY() > getInfo().getMapData().getMapSize().getY() / 2) {
                    leg = 1;
                } else {
                    leg = 2;
                }
            } else {
                if (base.getY() > getInfo().getMapData().getMapSize().getY() / 2) {
                    leg = 0;
                } else {
                    leg = 3;
                }
            }
        }

        if (searching) {
            patrol(leader, aliveUnits.toArray(new BotUnit[0]));
        }

        if (!attacking && getVisibleEnemies().size() != 0) {
            attacking = true;
            searching = false;
        }

        if (attacking) {
            commandEachUnit(this::attack, aliveUnits.toArray(new BotUnit[0]));
        }
    }

    private void attack(BotUnit attacker) {
        if (attacker.getWeaponCooldown() > 0f) {
            if (needCooling) {
                if (target.isAliveAndVisible()) {
                    moveUnits(retreatPoint(attacker.getPosition(), target.getPosition()), attacker);
                }
                needCooling = false;
            } else if (!target.isAliveAndVisible()) {
                // target down or out of sight
                attacking = false;
                searching = true;
            }
        } else if (getVisibleEnemies().size() != 0) {
            target = getVisibleEnemies().stream().min(Comparator.comparing(BotUnit::getHealth)).get();
            attackTarget(target, attacker);
            needCooling = true;
        }
    }

    private void patrol(BotUnit leader, BotUnit... all) {
        if (!leader.getOrders().isEmpty()) {
            return;
        }

        int border = 4;
        int maxX = getInfo().getMapData().getMapSize().getX() - border;
        int minX = border;
        int maxY = getInfo().getMapData().getMapSize().getY() - border;
        int minY = border;

        switch (leg) {
            case 0:
                moveUnits(Vec2.of(maxX, maxY), all);
                break;

            case 1:
                moveUnits(Vec2.of(maxX, minY), all);
                break;

            case 2:
                moveUnits(Vec2.of(minX, minY), all);
                break;

            case 3:
                moveUnits(Vec2.of(minX, maxY), all);
                break;
        }

        leg = ++leg % 4;
    }

    private Vec2 retreatPoint(Vec2 self, Vec2 enemy) {
        float xDist = (enemy.getX() - self.getX());
        float yDist = (enemy.getY() - self.getY());
        float mag = (float) Math.sqrt(xDist * xDist + yDist * yDist);
        float x = self.getX() - xDist / mag * 5;
        float y = self.getY() - yDist / mag * 5;
        return Vec2.of(x, y);
    }

    private void commandEachUnit(Consumer<BotUnit> cmd, BotUnit... units) {
        Arrays.stream(units).forEach(cmd::accept);
    }
}
