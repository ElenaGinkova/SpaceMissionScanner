package space;

import space.algorithm.Rijndael;
import space.algorithm.SymmetricBlockCipher;
import space.comparator.MissionCostComparator;
import space.exception.CipherException;
import space.mission.Mission;
import space.mission.MissionStatus;
import space.parser.MissionCSVParser;
import space.parser.Parser;
import space.parser.RocketCSVParser;
import space.rocket.Rocket;
import space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static space.parser.CSVParserUtils.parseCountryFromLocation;
import static space.validation.SpaceScannerValidations.validateNumber;
import static space.validation.SpaceScannerValidations.validateOutputStream;
import static space.validation.SpaceScannerValidations.validatePeriod;
import static space.validation.SpaceScannerValidations.validateSuccessfulMissionInInterval;

public class MJTSpaceScanner implements SpaceScannerAPI {
    private final List<Mission> missions;
    private final List<Rocket> rockets;
    private final SymmetricBlockCipher cipher;

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        this.cipher = new Rijndael(secretKey);
        this.missions = new ArrayList<>();
        this.rockets = new ArrayList<>();
        deserialize(missionsReader, new MissionCSVParser(), missions);
        deserialize(rocketsReader, new RocketCSVParser(), rockets);
    }

    @Override
    public Collection<Mission> getAllMissions() {
        return List.copyOf(missions);
    }

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        if (missionStatus == null) {
            throw new IllegalArgumentException("Mission Status cannot be null");
        }
        List<Mission> missionsWithStatus = new ArrayList<>();
        for (Mission mission : missions) {
            if (mission.missionStatus() == missionStatus) {
                missionsWithStatus.add(mission);
            }
        }
        return List.copyOf(missionsWithStatus);
    }

    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        if (missions.isEmpty()) {
            return "";
        }

        return keyWithMaxValue(mapSuccessfulCompanyMissionsCount(from, to), "");
    }

    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        if (missions.isEmpty()) {
            return Collections.emptyMap();
        }
        return missions.stream()
            .collect(Collectors.groupingBy(
                m -> parseCountryFromLocation(m.location()), Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        validateNumber(n);
        if (missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("Mission status or Rocket status cannot be null");
        }

        return getSortedMissions(missionStatus, rocketStatus, new MissionCostComparator(), n);
    }

    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        Map<String, Map<String, Long>> companyLocationsCnt =
            missions.stream()
                .collect(Collectors.groupingBy(
                    Mission::company,
                    Collectors.groupingBy(Mission::location, Collectors.counting())));

        return getMostDesiredLocationPerCompany(companyLocationsCnt);
    }

    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        Map<String, String> result = new HashMap<>();
        Map<String, Map<String, Integer>> counts = getCompaniesLocationsCount(from, to);
        for (var entry : counts.entrySet()) {
            String location = keyWithMaxValue(entry.getValue(), "");
            if (!location.isEmpty()) {
                result.put(entry.getKey(), location);
            }
        }
        return Map.copyOf(result);
    }

    @Override
    public Collection<Rocket> getAllRockets() {
        return List.copyOf(rockets);
    }

    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        validateNumber(n);
        return rockets.stream()
            .filter(m -> m.height().isPresent())
            .sorted(Comparator.comparingDouble((Rocket r) -> r.height().orElseThrow()).reversed())
            .limit(n)
            .toList();
    }

    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        return rockets.stream()
            .collect(Collectors.toMap(
                Rocket::name,
                Rocket::wiki
            ));
    }

    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                          RocketStatus rocketStatus) {
        validateNumber(n);
        if (missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("Mission status or Rocket status cannot be null");
        }

        List<Mission> mostExpensiveMissions =
            getSortedMissions(missionStatus, rocketStatus, new MissionCostComparator().reversed(), n);

        Set<String> res = new HashSet<>();
        for (Mission mission : mostExpensiveMissions) {
            res.add(mission.detail().rocketName());
        }

        return rockets.stream()
            .filter(r -> res.contains(r.name()))
            .filter(r -> r.wiki().isPresent())
            .map(r -> r.wiki().get())
            .toList();
    }

    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        validateOutputStream(outputStream);
        validatePeriod(from, to);

        Rocket mostRealiableRocket = getMostReliableRocket(from, to);
        String rocketName = (mostRealiableRocket == null) ? "" : mostRealiableRocket.name();
        byte[] bytes = rocketName.getBytes(StandardCharsets.UTF_8);

        try (InputStream in = new ByteArrayInputStream(bytes)) {
            cipher.encrypt(in, outputStream);
        } catch (IOException e) {
            throw new CipherException("Failed to close output stream", e);
        }
    }

    private <T> void deserialize(Reader reader, Parser<T> parser, Collection<T> collection) {
        if (reader == null) {
            collection = Collections.emptyList();
            return;
        }

        BufferedReader bufferedReader = new BufferedReader(reader);

        try {
            bufferedReader.readLine();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                collection.add(parser.deserialize(line));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from a file", e);
        }
    }

    private Map<String, Integer> mapSuccessfulCompanyMissionsCount(LocalDate from, LocalDate to) {
        return missions.stream()
            .filter(m -> validateSuccessfulMissionInInterval(m, from, to))
            .collect(Collectors.groupingBy(
                Mission::company, Collectors.summingInt(x -> 1)));
    }

    private <K> K keyWithMaxValue(Map<K, Integer> counts, K defaultValue) {
        return counts.entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(defaultValue);
    }

    private Map<String, Map<String, Integer>> getCompaniesLocationsCount(LocalDate from, LocalDate to) {
        return missions.stream()
            .filter(m -> validateSuccessfulMissionInInterval(m, from, to))
            .collect(Collectors.groupingBy(
                Mission::company,
                Collectors.groupingBy(Mission::location, Collectors.summingInt(x -> 1))
            ));
    }

    private Map<String, String> getMostDesiredLocationPerCompany(Map<String, Map<String, Long>> companyLocationsCnt) {
        Map<String, String> res = new HashMap<>();
        for (Map.Entry<String, Map<String, Long>> entry : companyLocationsCnt.entrySet()) {
            String location =
                entry.getValue().entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .orElseThrow().getKey();

            res.put(entry.getKey(), location);
        }
        return res;
    }

    private List<Mission> getSortedMissions(MissionStatus missionStatus, RocketStatus rocketStatus,
                                            Comparator<Mission> comp, int limit) {
        Stream<Mission> stream = missions.stream()
            .filter(m -> m.missionStatus() == missionStatus && m.rocketStatus() == rocketStatus)
            .filter(m -> m.cost().isPresent())
            .sorted(comp);

        return (limit != -1 ? stream.limit(limit) : stream).toList();
    }

    private Map<String, Long> rocketsCount(MissionStatus status, LocalDate from, LocalDate to) {
        Stream<Mission> result = missions.stream();
        if (status != null) {
            result =
                result.filter(m -> m.missionStatus() == status);
        }
        return
            result
                .filter(r -> !r.date().isBefore(from) && !r.date().isAfter(to))
                .collect(Collectors.groupingBy(
                    m -> m.detail().rocketName(), Collectors.counting()));
    }

    private Rocket getMostReliableRocket(LocalDate from, LocalDate to) {
        Map<String, Long> rocketSuccessCount = rocketsCount(MissionStatus.SUCCESS, from, to);
        Map<String, Long> rocketCount = rocketsCount(null, from, to);
        Rocket rocket = null;
        double bestReliability = -1.0;
        for (Rocket r : rockets) {
            long successCount = rocketSuccessCount.getOrDefault(r.name(), 0L);
            long totalCount = rocketCount.getOrDefault(r.name(), 0L);
            long failureCount = totalCount - successCount;
            double reliability = 0.0;
            if (totalCount != 0) {
                reliability = (double) (2 * successCount + failureCount) / (2 * totalCount);
            }
            if (reliability > bestReliability) {
                bestReliability = reliability;
                rocket = r;
            }
        }
        return rocket;
    }
}
