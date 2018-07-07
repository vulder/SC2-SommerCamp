package de.uni_passau.fim.sommercamp.sc2.bots;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import de.uni_passau.fim.sommercamp.sc2.BaseBot;
import de.uni_passau.fim.sommercamp.sc2.BotUnit;

@SuppressWarnings("ALL")
public class DemoBot extends BaseBot {

    public DemoBot(S2Client client) {
        super(client);
    }

    // Variables here
    private String name = "DemoBot";
    private int state = 0;

    @Override
    protected void onStep() {
        observe();

        move();

        attack();
    }

    private void attack() {

    }

    private void move() {

    }

    private void observe() {

    }

    @Override
    public String toString() {
        return "DemoBot<" + name + ">";
    }

    private void arrays() {

        int[] intArray = new int[10];
        // equivalent
        int[] intArray2 =
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        System.out.println(intArray[9]);
        intArray[0] = 5;
        intArray[9] = 7;
        intArray[10] = 7; // Error!
        System.out.println(intArray[9]);

        // iterating an array
        for (int i = 0; i < intArray.length; i++) {
            System.out.println(intArray[i]);
        }

    }

    private void controlFlow() {
        // bot logic here
        if (state == 0) {
            System.out.println("State is 0");
        } else {
            System.out.println("State is not 0");
        }


        for (int i = 0; i < 10; i++) {
            System.out.println("Loop " + i);
        }

        // equivalent
        int i = 0;
        while (i < 10) {
            System.out.println("Loop " + i);
            i = i + 1;
        }


        for (BotUnit unit : getUnits()) {
            System.out.println(unit.toString());
        }
    }

    private Point2d vectorAdd(Point2d a, Point2d b) {
        float x = a.getX() + b.getX();
        float y = a.getY() + b.getY();
        Point2d sum = Point2d.of(x, y);
        return sum;
    }

    private void printPoint(Point2d p) {
        System.out.println(
            "Point[x=" + p.getX() +
            ", y=" + p.getY() + "]");
    }
}
