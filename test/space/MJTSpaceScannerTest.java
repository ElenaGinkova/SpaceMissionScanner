package space;

import space.algorithm.Rijndael;
import space.exception.CipherException;
import space.exception.TimeFrameMismatchException;
import space.mission.Detail;
import space.mission.Mission;
import space.mission.MissionStatus;
import space.rocket.Rocket;
import space.rocket.RocketStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MJTSpaceScannerTest {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int KEY_SIZE_IN_BITS = 128;
    private static MJTSpaceScanner scanner;
    private static MJTSpaceScanner scanner2;
    private static SecretKey secretKey;

    private static final String MISSIONS_CSV = """
        Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
        0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
        1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        2,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Tue Aug 04, 2020",Starship Prototype | 150 Meter Hop,StatusActive,,Success
        3,Roscosmos,"Site 200/39, Baikonur Cosmodrome, Kazakhstan","Thu Jul 30, 2020",Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,"65.0 ",Success
        4,ULA,"SLC-41, Cape Canaveral AFS, Florida, USA","Thu Jul 30, 2020",Atlas V 541 | Perseverance,StatusRetired,"145.0 ",Failure
        """;

    private static final String ROCKETS_CSV = """
        "",Name,Wiki,Rocket Height
        0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m
        1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m
        2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m
        3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m
        4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m
        """;

    private static final String MISSIONS2_CSV = """
        Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
        2808,RVSN USSR,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Tue Jun 29, 1976",Voskhod | Cosmos 835,StatusRetired,,Success
        2812,RVSN USSR,"Site 43/3, Plesetsk Cosmodrome, Russia","Wed Jun 16, 1976",Voskhod | Cosmos 833,StatusRetired,,Success
        4277,US Navy,"LC-18A, Cape Canaveral AFS, Florida, USA","Fri Sep 18, 1959",Vanguard | Vanguard 3,StatusRetired,,Partial Failure
        4285,US Navy,"LC-18A, Cape Canaveral AFS, Florida, USA","Mon Jun 22, 1959",Vanguard | Vanguard SLV-6,StatusRetired,"50.0",Failure
        4288,US Navy,"LC-18A, Cape Canaveral AFS, Florida, USA","Tue Apr 14, 1959",Vanguard | Vanguard SLV-5,StatusRetired,"40.0",Failure
        4278,US Air Force,"SLC-17A, Cape Canaveral AFS, Florida, USA","Thu Sep 17, 1959",Thor DM-18 Able-II | Transit 1A,StatusRetired,"60.0",Failure
        """;

    private static final String ROCKETS2_CSV = """
        "",Name,Wiki,Rocket Height
        4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m
        12,Voskhod,https://en.wikipedia.org/wiki/Voskhod_(rocket),31.0 m
        380,Thor DM-18 Able-II,https://en.wikipedia.org/wiki/Thor-Able,
        """;

    private static final String ROCKETS3_CSV = """
        "",Name,Wiki,Rocket Height
        0,Falcon 9 Block 5,https://en.wikipedia.org/wiki/Falcon_9,70.0 m
        1,Long March 2D,https://en.wikipedia.org/wiki/Long_March_2D,41.1 m
        2,Starship Prototype,https://en.wikipedia.org/wiki/SpaceX_Starship,50.0 m
        3,Proton-M/Briz-M,https://en.wikipedia.org/wiki/Proton-M,58.2 m
        4,Atlas V 541,https://en.wikipedia.org/wiki/Atlas_V,58.3 m
        """;

    private static final List<Mission> EXPECTED_MISSIONS = List.of(
        new Mission("0", "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA", LocalDate.of(2020, 8, 7),
            new Detail("Falcon 9 Block 5", "Starlink V1 L9 & BlackSky"), RocketStatus.STATUS_ACTIVE, Optional.of(50.0),
            MissionStatus.SUCCESS),
        new Mission("1", "CASC", "Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China", LocalDate.of(2020, 8, 6),
            new Detail("Long March 2D", "Gaofen-9 04 & Q-SAT"), RocketStatus.STATUS_ACTIVE, Optional.of(29.75),
            MissionStatus.SUCCESS),
        new Mission("2", "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA", LocalDate.of(2020, 8, 4),
            new Detail("Starship Prototype", "150 Meter Hop"), RocketStatus.STATUS_ACTIVE, Optional.empty(),
            MissionStatus.SUCCESS),
        new Mission("3", "Roscosmos", "Site 200/39, Baikonur Cosmodrome, Kazakhstan", LocalDate.of(2020, 7, 30),
            new Detail("Proton-M/Briz-M", "Ekspress-80 & Ekspress-103"), RocketStatus.STATUS_ACTIVE, Optional.of(65.0),
            MissionStatus.SUCCESS),
        new Mission("4", "ULA", "SLC-41, Cape Canaveral AFS, Florida, USA", LocalDate.of(2020, 7, 30),
            new Detail("Atlas V 541", "Perseverance"), RocketStatus.STATUS_RETIRED, Optional.of(145.0),
            MissionStatus.FAILURE)
    );

    private static final List<Rocket> EXPECTED_ROCKETS = List.of(
        new Rocket("0", "Tsyklon-3", Optional.of("https://en.wikipedia.org/wiki/Tsyklon-3"),
            Optional.of(39.0)
        ),
        new Rocket("1", "Tsyklon-4M", Optional.of("https://en.wikipedia.org/wiki/Cyclone-4M"),
            Optional.of(38.7)
        ),
        new Rocket("2", "Unha-2", Optional.of("https://en.wikipedia.org/wiki/Unha"),
            Optional.of(28.0)
        ),
        new Rocket("3", "Unha-3", Optional.of("https://en.wikipedia.org/wiki/Unha"),
            Optional.of(32.0)
        ),
        new Rocket("4", "Vanguard", Optional.of("https://en.wikipedia.org/wiki/Vanguard_(rocket)"),
            Optional.of(23.0)
        )
    );

    @BeforeAll
    public static void setUp() throws NoSuchAlgorithmException {
        Reader missionReader = new StringReader(MISSIONS_CSV);
        Reader rocketReader = new StringReader(ROCKETS_CSV);

        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(KEY_SIZE_IN_BITS);
        secretKey = keyGenerator.generateKey();

        scanner = new MJTSpaceScanner(missionReader, rocketReader, secretKey);
        scanner2 = new MJTSpaceScanner(new StringReader(MISSIONS2_CSV), new StringReader(ROCKETS2_CSV), secretKey);
    }

    @Test
    public void testGetAllMissions() {
        Collection<Mission> missions = scanner.getAllMissions();
        assertIterableEquals(EXPECTED_MISSIONS, missions);
    }

    @Test
    public void testGetAllMissionsWithStatusSuccess() {
        Collection<Mission> missions = scanner.getAllMissions(MissionStatus.SUCCESS);
        List<Mission> expectedMissions = EXPECTED_MISSIONS.subList(0, 4);
        assertIterableEquals(expectedMissions, missions);
    }

    @Test
    public void testGetAllMissionsWithStatusSuccessWithNullStatus() {
        assertThrows(IllegalArgumentException.class, () -> scanner.getAllMissions(null));
    }

    @Test
    public void testGetCompanyWithMostSuccessfulMissions() {
        LocalDate from = LocalDate.of(2020, 8, 4);
        LocalDate to = LocalDate.of(2020, 8, 8);
        String company = scanner.getCompanyWithMostSuccessfulMissions(from, to);
        assertEquals("SpaceX", company);
    }

    @Test
    public void testGetCompanyWithMostSuccessfulMissionsInvalidInterval() {
        LocalDate from = LocalDate.of(2020, 8, 8);
        LocalDate to = LocalDate.of(2020, 8, 4);
        assertThrows(TimeFrameMismatchException.class, () -> scanner.getCompanyWithMostSuccessfulMissions(from, to));
    }

    @Test
    public void getCompanyWithMostSuccessfulMissionsWithNullMissions() {
        MJTSpaceScanner scanner2 = new MJTSpaceScanner(null, null, secretKey);
        LocalDate from = LocalDate.of(2020, 8, 4);
        LocalDate to = LocalDate.of(2020, 8, 5);
        assertEquals("", scanner2.getCompanyWithMostSuccessfulMissions(from, to));
    }

    @Test
    public void getMissionsPerCountryWithNullMissions() {
        MJTSpaceScanner scanner2 = new MJTSpaceScanner(null, null, secretKey);
        assertTrue(scanner2.getMissionsPerCountry().isEmpty());
    }

    @Test
    public void getMissionsPerCountry() {
        Map<String, Collection<Mission>> missionsPerCountry = scanner.getMissionsPerCountry();
        Map<String, Collection<Mission>> expectedMissionsPerCountry = new HashMap<>();
        expectedMissionsPerCountry.put("USA", new ArrayList<Mission>());
        expectedMissionsPerCountry.put("China", new ArrayList<Mission>());
        expectedMissionsPerCountry.put("Kazakhstan", new ArrayList<Mission>());

        expectedMissionsPerCountry.get("USA").add(EXPECTED_MISSIONS.get(0));
        expectedMissionsPerCountry.get("USA").add(EXPECTED_MISSIONS.get(2));
        expectedMissionsPerCountry.get("USA").add(EXPECTED_MISSIONS.get(4));
        expectedMissionsPerCountry.get("China").add(EXPECTED_MISSIONS.get(1));
        expectedMissionsPerCountry.get("Kazakhstan").add(EXPECTED_MISSIONS.get(3));

        assertIterableEquals(missionsPerCountry.get("USA"), expectedMissionsPerCountry.get("USA"));
        assertIterableEquals(missionsPerCountry.get("China"), expectedMissionsPerCountry.get("China"));
        assertIterableEquals(missionsPerCountry.get("Kazakhstan"), expectedMissionsPerCountry.get("Kazakhstan"));
    }

    @Test
    public void testGetTopNLeastExpensiveMissions() {
        List<Mission> res = scanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE);
        List<Mission> expectedMissions = new ArrayList<>();
        expectedMissions.add(EXPECTED_MISSIONS.get(1));
        expectedMissions.add(EXPECTED_MISSIONS.get(0));

        assertIterableEquals(expectedMissions, res);
    }

    @Test
    public void testGetTopNLeastExpensiveMissionsWithNullStatus() {
        assertThrows(IllegalArgumentException.class,
            () -> scanner.getTopNLeastExpensiveMissions(2, null, RocketStatus.STATUS_ACTIVE));
        assertThrows(IllegalArgumentException.class,
            () -> scanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, null));
    }

    @Test
    public void testGetMostDesiredLocationForMissionsPerCompany() {
        Map<String, String> expected = Map.of(
            "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA",
            "CASC", "Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China",
            "Roscosmos", "Site 200/39, Baikonur Cosmodrome, Kazakhstan",
            "ULA", "SLC-41, Cape Canaveral AFS, Florida, USA"
        );
        Map<String, String> output = scanner.getMostDesiredLocationForMissionsPerCompany();
        assertEquals(expected, output);
    }


    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompany() {
        LocalDate from = LocalDate.of(2020, 7, 30);
        LocalDate to = LocalDate.of(2020, 8, 7);

        Map<String, String> expected = Map.of(
            "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA",
            "CASC", "Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China",
            "Roscosmos", "Site 200/39, Baikonur Cosmodrome, Kazakhstan"
        );

        Map<String, String> output = scanner.getLocationWithMostSuccessfulMissionsPerCompany(from, to);

        assertEquals(expected, output);
        assertFalse(output.containsKey("ULA"));
    }

    @Test
    void testGetAllRockets() {
        assertIterableEquals(EXPECTED_ROCKETS, scanner.getAllRockets());
    }

    @Test
    void testGetTopNTallestRockets() {
        List<Rocket> top3 = scanner.getTopNTallestRockets(3);

        List<Rocket> expected = new ArrayList<>();
        expected.add(EXPECTED_ROCKETS.get(0));
        expected.add(EXPECTED_ROCKETS.get(1));
        expected.add(EXPECTED_ROCKETS.get(3));

        assertIterableEquals(expected, top3);
    }

    @Test
    void testGetWikiPageForRocket() {
        Map<String, Optional<String>> expected = Map.of(
            "Tsyklon-3", Optional.of("https://en.wikipedia.org/wiki/Tsyklon-3"),
            "Tsyklon-4M", Optional.of("https://en.wikipedia.org/wiki/Cyclone-4M"),
            "Unha-2", Optional.of("https://en.wikipedia.org/wiki/Unha"),
            "Unha-3", Optional.of("https://en.wikipedia.org/wiki/Unha"),
            "Vanguard", Optional.of("https://en.wikipedia.org/wiki/Vanguard_(rocket)")
        );

        Map<String, Optional<String>> actual = scanner.getWikiPageForRocket();

        assertEquals(expected, actual);
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissions() {
        List<String> result = scanner2.getWikiPagesForRocketsUsedInMostExpensiveMissions(
            2, MissionStatus.FAILURE, RocketStatus.STATUS_RETIRED
        );

        assertEquals(Set.of(
                "https://en.wikipedia.org/wiki/Vanguard_(rocket)",
                "https://en.wikipedia.org/wiki/Thor-Able"),
            new HashSet<>(result)
        );
    }

    @Test
    void testSaveMostReliableRocket() throws CipherException {
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        scanner2.saveMostReliableRocket(
            encryptedOut,
            LocalDate.of(2020, 7, 30),
            LocalDate.of(2020, 8, 7));

        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        Rijndael rijndael = new Rijndael(secretKey);
        rijndael.decrypt(
            new ByteArrayInputStream(encryptedOut.toByteArray()),
            decryptedOut
        );

        assertEquals("Vanguard", decryptedOut.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testSaveMostReliableRocketWithNoOutput() {
        assertThrows(IllegalArgumentException.class, () -> scanner2.saveMostReliableRocket(
            null,
            LocalDate.of(2020, 7, 30),
            LocalDate.of(2020, 8, 7)));
    }
}
