package com.breezzo.sc2.matchups.loader.domain;

import java.time.Instant;
import java.util.List;

/**
 * @author breezzo
 * @since 3/2/19.
 */
public class MatchupResult {
    private Long matchupId;
    private String mapName;
    private long durationMs;
    private Instant startedAt;
    private List<GamerInfo> gamers;

    public Long getMatchupId() {
        return matchupId;
    }

    public void setMatchupId(Long matchupId) {
        this.matchupId = matchupId;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public List<GamerInfo> getGamers() {
        return gamers;
    }

    public void setGamers(List<GamerInfo> gamers) {
        this.gamers = gamers;
    }

    public static class GamerInfo {
        private String username;
        private String race;
        private boolean win;

        public boolean isWin() {
            return win;
        }

        public void setWin(boolean win) {
            this.win = win;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRace() {
            return race;
        }

        public void setRace(String race) {
            this.race = race;
        }
    }
}
