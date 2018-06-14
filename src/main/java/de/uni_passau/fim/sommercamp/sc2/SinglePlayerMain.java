package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.api.controller.S2Controller;
import com.github.ocraft.s2client.api.rx.Responses;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.response.ResponseLeaveGame;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static com.github.ocraft.s2client.api.S2Client.starcraft2Client;
import static com.github.ocraft.s2client.api.controller.S2Controller.starcraft2Game;
import static com.github.ocraft.s2client.protocol.game.ComputerPlayerSetup.computer;
import static com.github.ocraft.s2client.protocol.game.PlayerSetup.participant;
import static com.github.ocraft.s2client.protocol.game.Race.PROTOSS;
import static com.github.ocraft.s2client.protocol.request.RequestCreateGame.createGame;

public class SinglePlayerMain {

    public static void main(String[] args) throws URISyntaxException {
        S2Controller game = starcraft2Game().launch();
        S2Client client = starcraft2Client().connectTo(game).traced(true).start();

        BaseBot player = Util.getBot(client);

        client.request(createGame()
                .onLocalMap(LocalMap.of(Paths.get(ClassLoader.getSystemResource("Lava Flow.SC2Map").toURI())))
                .withPlayerSetup(participant(), computer(/*TODO*/PROTOSS, Difficulty.EASY)));

        client.responseStream().takeWhile(Responses.isNot(ResponseLeaveGame.class)).subscribe(player::handle);
        client.await();
    }
}