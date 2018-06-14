package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.game.raw.StartRaw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GameInfo {
    public final List<PlayerInfo> playersInfo;
    public final String mapName;
    public final StartRaw mapData;

    GameInfo(Collection<PlayerInfo> playersInfo, String mapName, StartRaw mapData) {
        this.playersInfo = Collections.unmodifiableList(new ArrayList<>(playersInfo));
        this.mapName = mapName;
        this.mapData = mapData;
    }
}
