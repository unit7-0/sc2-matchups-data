package com.breezzo.sc2.matchups.data.generator;

/**
 * @author breezzo
 * @since 3/2/19.
 */
public interface MatchupsUploader {
    void upload(int matchupsCount, MatchupsGenerator generator);
}
