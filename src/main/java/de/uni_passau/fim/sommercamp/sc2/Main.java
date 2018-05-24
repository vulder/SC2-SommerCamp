package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.api.S2Client;
import com.github.ocraft.s2client.api.controller.S2Controller;
import com.github.ocraft.s2client.api.rx.Responses;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.response.ResponseCreateGame;
import com.github.ocraft.s2client.protocol.response.ResponseJoinGame;
import com.github.ocraft.s2client.protocol.response.ResponseLeaveGame;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.PointI;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static com.github.ocraft.s2client.api.S2Client.starcraft2Client;
import static com.github.ocraft.s2client.api.controller.S2Controller.starcraft2Game;
import static com.github.ocraft.s2client.protocol.action.Action.action;
import static com.github.ocraft.s2client.protocol.action.raw.ActionRawCameraMove.cameraMove;
import static com.github.ocraft.s2client.protocol.action.raw.ActionRawUnitCommand.unitCommand;
import static com.github.ocraft.s2client.protocol.action.spatial.ActionSpatialUnitSelectionPoint.Type.TOGGLE;
import static com.github.ocraft.s2client.protocol.action.spatial.ActionSpatialUnitSelectionPoint.click;
import static com.github.ocraft.s2client.protocol.action.ui.ActionUiSelectArmy.selectArmy;
import static com.github.ocraft.s2client.protocol.data.Abilities.TRAIN_SCV;
import static com.github.ocraft.s2client.protocol.game.ComputerPlayerSetup.computer;
import static com.github.ocraft.s2client.protocol.game.PlayerSetup.participant;
import static com.github.ocraft.s2client.protocol.game.Race.PROTOSS;
import static com.github.ocraft.s2client.protocol.game.Race.TERRAN;
import static com.github.ocraft.s2client.protocol.request.RequestAction.actions;
import static com.github.ocraft.s2client.protocol.request.RequestCreateGame.createGame;
import static com.github.ocraft.s2client.protocol.request.RequestJoinGame.joinGame;
import static com.github.ocraft.s2client.protocol.request.RequestLeaveGame.leaveGame;

public class Main {

    public static void main(String[] args) throws URISyntaxException {
        S2Controller game = starcraft2Game().launch();
        S2Client client = starcraft2Client().connectTo(game).traced(true).start();

        client.request(createGame()
                .onLocalMap(LocalMap.of(Paths.get(ClassLoader.getSystemResource("Lava Flow.SC2Map").toURI())))
                .withPlayerSetup(participant(), computer(PROTOSS, Difficulty.MEDIUM)));

        client.responseStream()
                .takeWhile(Responses.isNot(ResponseLeaveGame.class))
                .subscribe(response -> {
                    response.as(ResponseCreateGame.class).ifPresent(r -> client.request(joinGame().as(TERRAN)));
                    response.as(ResponseJoinGame.class).ifPresent(r -> {
                        client.request(actions().of(
                                action().raw(unitCommand().forUnits(Tag.of(1234L)).useAbility(TRAIN_SCV)),
                                action().raw(cameraMove().to(Point.of(10, 10))),
                                action().featureLayer(click().on(PointI.of(15, 10)).withMode(TOGGLE)),
                                action().ui(selectArmy().add())
                        ));
                        client.request(leaveGame());
                    });
                });

        client.await();
    }
}