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

    private final List<PlayerInfo> playersInfo;
    private final String mapName;
    private final StartRaw mapData;

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

    /**
     * Gets information about all players.
     *
     * @return a list of raw info objects about the players
     * @see PlayerInfo#getPlayerType()
     * @see PlayerInfo#getDifficulty()
     */
    public List<PlayerInfo> getPlayersInfo() {
        return playersInfo;
    }

    /**
     * Gets the name of the map of the current game.
     *
     * @return the name of the map
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * Gets information about the map.
     *
     * Map bounds are exclusive (i.e. for a map with dimensions 32x32, 32 is not a valid position)
     *
     * @return raw data about the map
     * @see StartRaw#getMapSize()
     */
    public StartRaw getMapData() {
        return mapData;
    }
}
