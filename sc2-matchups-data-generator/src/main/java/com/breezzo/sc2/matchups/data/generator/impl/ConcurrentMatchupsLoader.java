package com.breezzo.sc2.matchups.data.generator.impl;

import com.breezzo.sc2.matchups.data.generator.MatchupsGenerator;
import com.breezzo.sc2.matchups.data.generator.MatchupsSender;
import com.breezzo.sc2.matchups.data.generator.MatchupsUploader;
import com.breezzo.sc2.matchups.data.generator.domain.MatchupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Component
public class ConcurrentMatchupsLoader implements MatchupsUploader {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentMatchupsLoader.class);

    private final int batchSize = 1000;
    private final int concurrencyLevel = Runtime.getRuntime().availableProcessors();
    private final Executor uploadExecutor = Executors.newFixedThreadPool(concurrencyLevel);

    @Autowired
    private MatchupsSender matchupsSender;

    @Override
    public void upload(int matchupsCount, MatchupsGenerator generator) {
        AtomicInteger matchupsRemain = new AtomicInteger(matchupsCount);
        CompletableFuture<Void>[] futures = new CompletableFuture[concurrencyLevel];
        for (int i = 0; i < concurrencyLevel; i++) {
            futures[i] = CompletableFuture.runAsync(() -> upload(matchupsRemain, generator), uploadExecutor);
        }
        CompletableFuture.allOf(futures)
            .thenRun(() -> {
                logger.info("Generation and uploading complete, matchups generated: {}", matchupsCount);
            })
            .exceptionally(e -> {
                logger.error("Generation failed", e);
                return null;
            });
        logger.debug("Generation started, matchups count: {}", matchupsCount);
    }

    private void upload(AtomicInteger matchupsRemain, MatchupsGenerator generator) {
        int localMatchupsRemain;
        while ((localMatchupsRemain = matchupsRemain.get()) > 0) {
            if (Thread.currentThread().isInterrupted()) {
                logger.info("Thread was interrupted, remain matchups: {}", matchupsRemain);
                break;
            }
            int nextBatchSize = Math.min(batchSize, localMatchupsRemain);
            if (!matchupsRemain.compareAndSet(localMatchupsRemain, localMatchupsRemain - nextBatchSize)) {
                continue;
            }

            Collection<MatchupResult> matchups = generator.generate(nextBatchSize);
            logger.trace("Sending {}", matchups);
            matchupsSender.send(matchups);
            logger.debug("Next {} sent, remain matchups: {}", nextBatchSize, matchupsRemain);
        }
    }
}
