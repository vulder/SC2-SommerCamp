package de.uni_passau.fim.sommercamp.sc2.bots;

import com.github.ocraft.s2client.api.S2Client;
import de.uni_passau.fim.sommercamp.sc2.BaseBot;
import de.uni_passau.fim.sommercamp.sc2.BotUnit;
import de.uni_passau.fim.sommercamp.sc2.util.Vec2;

import java.util.Comparator;

public class FleckBot extends BaseBot {

    private boolean searching = false;
    private boolean attacking = false;
    private boolean needCooling = false;

    private BotUnit runner;
    private BotUnit target;
    private int leg = 0;

    public FleckBot(S2Client client) {
        super(client);
    }

    @Override
    protected void onStep() {
        for (BotUnit u: getObservation().getDiedInLastStep()) {
          if (u.isMine()) {
              runner = getUnits().get(0);
          }
        }

        if (runner != null && !runner.isAliveAndVisible()) {
            return;
        }

        if (!searching & !attacking) {
            searching = true;

            runner = getUnits().get(0);
            Vec2 base = runner.getPosition();
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
            patrol();
        }

        if (!attacking && getVisibleEnemies().size() != 0) {
            attacking = true;
            searching = false;
        }

        if (attacking) {
            attack();
        }

        if (getUnits().stream().anyMatch(u -> u.getHealth() != u.getMaxHealth())) {
            getUnits().stream().filter(u -> u.getType().getUnitTypeId() == 1731).max(Comparator.comparing(BotUnit::getEnergy))
                    .ifPresent(u -> getUnits().stream().filter(t -> t.getHealth() != t.getMaxHealth()).findFirst().ifPresent(u::heal));
        }
    }

    private void attack() {
        if (runner.getWeaponCooldown() > 0f) {
            if (needCooling) {
                if (target.isAliveAndVisible()) {
                     moveUnits(retreatPoint(runner.getPosition(), target.getPosition()), runner);
                }
                needCooling = false;
            } else if (!target.isAliveAndVisible()) {
                // target down or out of sight
                attacking = false;
                searching = true;
            }
        } else if (getVisibleEnemies().size() != 0) {
            target = getVisibleEnemies().stream().min(Comparator.comparing(BotUnit::getHealth)).get();
            attackTarget(target, runner);
            needCooling = true;
        }
    }

    private void patrol() {
        if (!runner.getOrders().isEmpty()) {
            return;
        }

        int border = 4;
        int maxX = getInfo().getMapData().getMapSize().getX() - border;
        int minX = border;
        int maxY = getInfo().getMapData().getMapSize().getY() - border;
        int minY = border;

        switch (leg) {
            case 0:
                moveUnits(Vec2.of(maxX, maxY), runner);
                break;

            case 1:
                moveUnits(Vec2.of(maxX, minY), runner);
                break;

            case 2:
                moveUnits(Vec2.of(minX, minY), runner);
                break;

            case 3:
                moveUnits(Vec2.of(minX, maxY), runner);
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
}
