package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.api.controller.S2Controller;
import com.github.ocraft.s2client.api.rx.Responses;

import java.io.IOException;

import static com.github.ocraft.s2client.api.S2Client.starcraft2Client;
import static com.github.ocraft.s2client.api.controller.S2Controller.starcraft2Game;
import static com.github.ocraft.s2client.protocol.game.ComputerPlayerSetup.computer;
import static com.github.ocraft.s2client.protocol.game.Difficulty.VERY_EASY;
import static com.github.ocraft.s2client.protocol.game.PlayerSetup.participant;
import static com.github.ocraft.s2client.protocol.game.Race.TERRAN;
import static com.github.ocraft.s2client.protocol.request.RequestCreateGame.createGame;
import static com.github.ocraft.s2client.protocol.response.ResponseType.LEAVE_GAME;

/**
 * Entry point for Bot vs Computer single player games.
 */
public class SinglePlayerMain {

    public static void main(String[] args) throws IOException {
        run("maps/Marines_2v2_d.SC2Map", "ExampleBot");
    }

    static void run(String map, String bot) throws IOException {
        S2Controller game = starcraft2Game().launch();
        S2Client client = starcraft2Client().connectTo(game).traced(true).start();

        BaseBot player = ReflectionUtil.getBotByName(bot, client);

        client.request(createGame().onLocalMap(ReflectionUtil.getMapFromName(map))
                .withPlayerSetup(participant(), computer(TERRAN, VERY_EASY)));

        client.responseStream().takeWhile(Responses.isNot(LEAVE_GAME)).subscribe(player::handle);
        client.await();
    }
}