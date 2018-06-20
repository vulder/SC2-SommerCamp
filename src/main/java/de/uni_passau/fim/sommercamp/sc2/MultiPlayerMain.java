package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.api.controller.S2Controller;
import com.github.ocraft.s2client.api.rx.Responses;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.MultiplayerOptions;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.github.ocraft.s2client.api.S2Client.starcraft2Client;
import static com.github.ocraft.s2client.api.controller.S2Controller.starcraft2Game;
import static com.github.ocraft.s2client.protocol.game.InterfaceOptions.interfaces;
import static com.github.ocraft.s2client.protocol.game.MultiplayerOptions.multiplayerSetupFor;
import static com.github.ocraft.s2client.protocol.game.PlayerSetup.participant;
import static com.github.ocraft.s2client.protocol.game.Race.TERRAN;
import static com.github.ocraft.s2client.protocol.request.RequestCreateGame.createGame;
import static com.github.ocraft.s2client.protocol.request.RequestJoinGame.joinGame;
import static com.github.ocraft.s2client.protocol.response.ResponseType.QUIT_GAME;
import static de.uni_passau.fim.sommercamp.sc2.Util.getMultipleBots;

public class MultiPlayerMain {

    public static void main(String[] args) throws URISyntaxException {
        run("Marines_2v2_d.SC2Map", "ExampleBot", "ExampleBot");
    }

    static void run(String map, String botA, String botB) throws URISyntaxException {

        S2Controller game01 = starcraft2Game().launch();
        S2Client client01 = starcraft2Client().connectTo(game01).traced(true).start();

        S2Controller game02 = starcraft2Game().launch();
        S2Client client02 = starcraft2Client().connectTo(game02).traced(true).start();

        client01.request(createGame()
                .onLocalMap(LocalMap.of(Paths.get(ClassLoader.getSystemResource(map).toURI())))
                .withPlayerSetup(participant(), participant()));

        List<BaseBot> players = getMultipleBots(Arrays.asList(botA, botB), Arrays.asList(client01, client02));
        MultiplayerOptions multiplayerOptions = multiplayerSetupFor(S2Controller.lastPort(), 2);

        client01.request(joinGame().as(TERRAN).use(interfaces().raw()).with(multiplayerOptions));
        client02.request(joinGame().as(TERRAN).use(interfaces().raw()).with(multiplayerOptions));

        client01.responseStream().takeWhile(Responses.isNot(QUIT_GAME)).subscribe(players.get(0)::handle);
        client02.responseStream().takeWhile(Responses.isNot(QUIT_GAME)).subscribe(players.get(1)::handle);

        client01.await();
        client02.await();
    }
}
