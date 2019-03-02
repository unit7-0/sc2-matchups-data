package com.breezzo.sc2.matchups.data.generator.impl;

import com.breezzo.sc2.matchups.data.generator.MatchupsGenerator;
import com.breezzo.sc2.matchups.data.generator.domain.MatchupResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Component
public class MatchupsGeneratorImpl implements MatchupsGenerator {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private static final List<String> MAP_POOL = List.of(
            "KERRIGAN'S CHAMPIONS",
            "Automaton LE",
            "Cyber Forest LE",
            "Kairos Junction LE",
            "King's Cove LE",
            "New Repugnancy LE",
            "Port Aleksander LE",
            "Year Zero LE"
    );
    private static final List<String> RACE_POOL = List.of("terran", "zerg", "protoss");
    private static final List<String> USER_POOL = generateUsernames();

    @Override
    public Collection<MatchupResult> generate(int matchupsCount) {
        List<MatchupResult> matchups = new ArrayList<>(matchupsCount);
        for (int i = 0; i < matchupsCount; i++) {
            matchups.add(generate());
        }
        return List.copyOf(matchups);
    }

    private MatchupResult generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        MatchupResult result = new MatchupResult();
        result.setMatchupId(ID_GENERATOR.getAndIncrement());
        result.setDurationMs(random.nextLong(180_000, 3600_000));
        result.setStartedAt(Instant.now().plusSeconds(random.nextLong(0, 300)));
        result.setMapName(MAP_POOL.get(random.nextInt(MAP_POOL.size())));

        MatchupResult.GamerInfo gamer1 = new MatchupResult.GamerInfo();
        gamer1.setRace(RACE_POOL.get(random.nextInt(RACE_POOL.size())));
        gamer1.setUsername("CraftyPuls");
        gamer1.setWin(random.nextBoolean());

        MatchupResult.GamerInfo gamer2 = new MatchupResult.GamerInfo();
        gamer2.setRace(RACE_POOL.get(random.nextInt(RACE_POOL.size())));
        gamer2.setUsername(USER_POOL.get(random.nextInt(USER_POOL.size())));
        gamer2.setWin(!gamer1.isWin());

        result.setGamers(List.of(gamer1, gamer2));

        return result;
    }

    private static List<String> generateUsernames() {
        String base = "mind";
        List<String> prefixes = List.of("over", "mega", "super", "my", "true", "fast");
        Set<String> usernames = new HashSet<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 15; i++) {
            int prefix = random.nextInt(prefixes.size());
            String username = prefixes.get(prefix) + "_" + base;
            if (usernames.contains(username)) {
                usernames.add(username + random.nextInt(1980, 2005));
            } else {
                usernames.add(username);
            }
        }

        return List.copyOf(usernames);
    }
}
