package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.api.controller.S2Controller;
import com.github.ocraft.s2client.api.rx.Responses;
import com.github.ocraft.s2client.protocol.game.MultiplayerOptions;

import java.io.IOException;
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
import static de.uni_passau.fim.sommercamp.sc2.ReflectionUtil.getMultipleBots;

/**
 * Entry point for Bot vs Bot multi player games.
 */
public class MultiPlayerMain {

    public static void main(String[] args) throws IOException {
        run("Marines_Marauder_4v2_d.SC2Map", Arrays.asList("NumberOneBot", "NumberOneBot"));
    }

    static void run(String map, List<String> bot) throws IOException {

        S2Controller game01 = starcraft2Game().launch();
        S2Client client01 = starcraft2Client().connectTo(game01).traced(true).start();

        S2Controller game02 = starcraft2Game().launch();
        S2Client client02 = starcraft2Client().connectTo(game02).traced(true).start();

        client01.request(createGame().onLocalMap(ReflectionUtil.getMapFromName(map))
                .withPlayerSetup(participant(), participant()));

        List<BaseBot> players = getMultipleBots(Arrays.asList(bot.get(0), bot.get(1)), Arrays.asList(client01, client02));
        MultiplayerOptions multiplayerOptions = multiplayerSetupFor(S2Controller.lastPort(), 2);

        client01.request(joinGame().as(TERRAN).use(interfaces().raw()).with(multiplayerOptions));
        client02.request(joinGame().as(TERRAN).use(interfaces().raw()).with(multiplayerOptions));

        client01.responseStream().takeWhile(Responses.isNot(QUIT_GAME)).subscribe(players.get(0)::handle);
        client02.responseStream().takeWhile(Responses.isNot(QUIT_GAME)).subscribe(players.get(1)::handle);

        client01.await();
        client02.await();
    }
}
