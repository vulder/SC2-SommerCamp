package de.uni_passau.fim.sommercamp.sc2;

import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.game.raw.StartRaw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * GameInfo contains static information about the current game.
 */
public class GameInfo {

    /**
     * Information about the players.
     *
     * @see PlayerInfo#getPlayerType()
     * @see PlayerInfo#getDifficulty()
     */
    public final List<PlayerInfo> playersInfo;

    /**
     * The name of the map of hte current game.
     */
    public final String mapName;

    /**
     * Information about the map.
     *
     * @see StartRaw#getMapSize()
     */
    public final StartRaw mapData;

    /**
     * Creates a new GameInfo based on the given data.
     *
     * @param playersInfo information about the players
     * @param mapName     the name of the map
     * @param mapData     information about the map
     */
    GameInfo(Collection<PlayerInfo> playersInfo, String mapName, StartRaw mapData) {
        this.playersInfo = Collections.unmodifiableList(new ArrayList<>(playersInfo));
        this.mapName = mapName;
        this.mapData = mapData;
    }
}
