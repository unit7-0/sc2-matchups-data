package com.breezzo.sc2.matchups.loader.repository.clickhouse;

import com.breezzo.sc2.matchups.loader.domain.MatchupResult;
import com.breezzo.sc2.matchups.loader.repository.MatchupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Repository
public class MatchupRepositoryImpl implements MatchupRepository {
    private static final String INSERT_SQL = "INSERT INTO sc2.matchups_buffer(" +
            " started_at, matchup_id," +
            " duration_ms, map_name," +
            " gamer_username, gamer_race," +
            " gamer_win)" +
            " VALUES(:started_at, :matchup_id," +
            " :duration_ms, :map_name," +
            " :gamer_username, :gamer_race," +
            " :gamer_win)";

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void saveResult(MatchupResult matchupResult) {
        for (MatchupResult.GamerInfo gamer: matchupResult.getGamers()) {
            MapSqlParameterSource params = toParameterSource(matchupResult, gamer);
            namedParameterJdbcTemplate.update(INSERT_SQL, params);
        }
    }

    private MapSqlParameterSource toParameterSource(MatchupResult matchupResult, MatchupResult.GamerInfo gamer) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("started_at", Timestamp.from(matchupResult.getStartedAt()))
                .addValue("matchup_id", matchupResult.getMatchupId())
                .addValue("duration_ms", matchupResult.getDurationMs())
                .addValue("map_name", matchupResult.getMapName())
                .addValue("gamer_username", gamer.getUsername())
                .addValue("gamer_race", gamer.getRace())
                .addValue("gamer_win", gamer.isWin() ? 1 : 0);
        return params;
    }

    @Override
    public void saveResult(List<MatchupResult> matchups) {
        if (matchups.isEmpty()) {
            return;
        }

        List<MapSqlParameterSource> sources = new ArrayList<>(matchups.size() * 2);
        for (MatchupResult result : matchups) {
            for (MatchupResult.GamerInfo gamer : result.getGamers()) {
                sources.add(toParameterSource(result, gamer));
            }
        }
        MapSqlParameterSource[] sourcesArray = new MapSqlParameterSource[sources.size()];
        sourcesArray = sources.toArray(sourcesArray);
        namedParameterJdbcTemplate.batchUpdate(INSERT_SQL, sourcesArray);
    }
}
