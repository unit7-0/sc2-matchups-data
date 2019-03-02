package com.breezzo.sc2.matchups.data.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Component
public class MatchupsDataGenerator {

    @Autowired
    private MatchupsGenerator matchupsGenerator;

    @Autowired
    private MatchupsUploader matchupsUploader;

    public void generateAndUpload(int matchupsCount) {
        matchupsUploader.upload(matchupsCount, matchupsGenerator);
    }
}
