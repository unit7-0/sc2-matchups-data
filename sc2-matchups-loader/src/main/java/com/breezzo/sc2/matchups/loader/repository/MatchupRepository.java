package com.breezzo.sc2.matchups.loader.repository;

import com.breezzo.sc2.matchups.loader.domain.MatchupResult;

import java.util.List;

/**
 * @author breezzo
 * @since 3/2/19.
 */
public interface MatchupRepository {
    void saveResult(MatchupResult matchupResult);

    void saveResult(List<MatchupResult> matchups);
}
