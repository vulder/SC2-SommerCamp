package de.uni_passau.fim.sommercamp.sc2.bots;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import de.uni_passau.fim.sommercamp.sc2.BaseBot;
import de.uni_passau.fim.sommercamp.sc2.BotUnit;

import java.util.Comparator;

public class ExampleBot extends BaseBot {

    private boolean searching = false;
    private boolean attacking = false;
    private boolean needCooling = false;

    private BotUnit runner;
    private BotUnit target;
    private int leg = 0;

    public ExampleBot(S2Client client) {
        super(client);
    }

    @Override
    protected void onStep() {
        if (runner != null && !runner.isAliveOrVisible()) {
            return;
        }

        if (!searching & !attacking) {
            searching = true;

            runner = getUnits().get(0);
            Point2d base = runner.getPosition();
            if (base.getX() > getInfo().mapData.getMapSize().getX() / 2) {
                if (base.getY() > getInfo().mapData.getMapSize().getY() / 2) {
                    leg = 1;
                } else {
                    leg = 2;
                }
            } else {
                if (base.getY() > getInfo().mapData.getMapSize().getY() / 2) {
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
    }

    private void attack() {
        if (runner.getWeaponCooldown() > 0f) {
            if (needCooling) {
                if (target.isAliveOrVisible()) {
                     moveUnits(retreatPoint(runner.getPosition(), target.getPosition()), runner);
                }
                needCooling = false;
            } else if (!target.isAliveOrVisible()) {
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
        int maxX = getInfo().mapData.getMapSize().getX() - border;
        int minX = border;
        int maxY = getInfo().mapData.getMapSize().getY() - border;
        int minY = border;

        switch (leg) {
            case 0:
                moveUnits(Point2d.of(maxX, maxY), runner);
                break;

            case 1:
                moveUnits(Point2d.of(maxX, minY), runner);
                break;

            case 2:
                moveUnits(Point2d.of(minX, minY), runner);
                break;

            case 3:
                moveUnits(Point2d.of(minX, maxY), runner);
                break;
        }

        leg = ++leg % 4;
    }

    private Point2d retreatPoint(Point2d self, Point2d enemy) {
        float xDist = (enemy.getX() - self.getX());
        float yDist = (enemy.getY() - self.getY());
        float mag = (float) Math.sqrt(xDist * xDist + yDist * yDist);
        float x = self.getX() - xDist / mag * 5;
        float y = self.getY() - yDist / mag * 5;
        return Point2d.of(x, y);
    }
}
