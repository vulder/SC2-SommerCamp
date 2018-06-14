package de.uni_passau.fim.sommercamp.sc2.bots;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import de.uni_passau.fim.sommercamp.sc2.BaseBot;

import static com.github.ocraft.s2client.protocol.game.Race.TERRAN;

public class ExampleBot extends BaseBot {

    public ExampleBot(S2Client client) {
        super(client, TERRAN);
    }

    @Override
    protected void onStep() {
        moveUnits(getIdleUnits(), Point2d.of(100, 100));
    }
}
