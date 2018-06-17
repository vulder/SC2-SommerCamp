package de.uni_passau.fim.sommercamp.sc2.bots;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;
import de.uni_passau.fim.sommercamp.sc2.BaseBot;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class ExampleBot extends BaseBot {

    private boolean searching = false;
    private boolean attacking = false;
    private boolean needCooling = false;

    private Tag runnerTag;
    private Tag targetTag;
    private int leg = 0;

    public ExampleBot(S2Client client) {
        super(client);
    }

    @Override
    protected void onStep() {
        if (!searching & !attacking) {
            searching = true;

            Unit runner = getUnits()[0];
            runnerTag = runner.getTag();
            Point base = runner.getPosition();
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

        if (!attacking && getVisibleEnemies().length != 0) {
            attacking = true;
            searching = false;
        }

        if (attacking) {
            attack();
        }
    }

    private void attack() {
        Optional<Unit> runnerOpt = findByTag(runnerTag);
        if (!runnerOpt.isPresent()) {
            return;
        }
        Unit runner = runnerOpt.get();

        if (runner.getWeaponCooldown().orElse(0f) > 0f) {
            Optional<Unit> target = findByTag(targetTag);
            if (needCooling) {
                target.ifPresent(t -> moveUnits(retreatPoint(runner.getPosition(), t.getPosition()), runner));
                needCooling = false;
            } else if (!target.isPresent()) {
                // target down or out of sight
                attacking = false;
                searching = true;
            }
        } else if (getVisibleEnemies().length != 0) {
            targetTag = Arrays.stream(getVisibleEnemies()).min(Comparator.comparing(e -> e.getHealth().orElse(1f))).get().getTag();
            attackTarget(targetTag, runner);
            needCooling = true;
        }
    }

    private void patrol() {
        Optional<Unit> runnerOpt = findByTag(runnerTag);
        if (!runnerOpt.isPresent() || !runnerOpt.get().getOrders().isEmpty()) {
            return;
        }
        Unit runner = runnerOpt.get();

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

    private Point2d retreatPoint(Point self, Point enemy) {
        float xDist = (enemy.getX() - self.getX());
        float yDist = (enemy.getY() - self.getY());
        float mag = (float) Math.sqrt(xDist * xDist + yDist * yDist);
        float x = self.getX() - xDist / mag * 5;
        float y = self.getY() - yDist / mag * 5;
        return Point2d.of(x, y);
    }
}
