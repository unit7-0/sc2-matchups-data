package com.breezzo.sc2.matchups.data.generator;

import com.breezzo.sc2.matchups.data.generator.domain.MatchupResult;

import java.util.Collection;

/**
 * @author breezzo
 * @since 3/2/19.
 */
public interface MatchupsSender {
    void send(Collection<MatchupResult> matchups);
}
