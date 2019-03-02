package com.breezzo.sc2.matchups.loader.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Document(indexName = "matchup-result", type = "matchup")
public class MatchupResultDocument {
    @Id
    private Long id;

    private String mapName;
    private long durationMs;

    private Timestamp startedAt;

    @Field(type = FieldType.Nested)
    private List<GamerInfo> gamers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
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
