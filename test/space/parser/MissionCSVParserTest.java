package space.parser;

import space.mission.Mission;
import space.mission.MissionStatus;
import space.rocket.RocketStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MissionCSVParserTest {
    MissionCSVParser parser = new MissionCSVParser();

    @Test
    public void testDeserializeWithAllElementsGiven() {
        String line =
            "1,CASC,\" Site 9401 (SLS - 2), Jiuquan Satellite Launch Center, China \",\" Thu Aug 06, 2020\",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\" 29.75 \",Partial Failure";
        Mission mission = parser.deserialize(line);
        assertCommonFields(mission, "1", "CASC", "Site 9401 (SLS - 2), Jiuquan Satellite Launch Center, China",
            LocalDate.of(2020, 8, 6), "Long March 2D", "Gaofen-9 04 & Q-SAT", RocketStatus.STATUS_ACTIVE,
            MissionStatus.PARTIAL_FAILURE);
        assertTrue(mission.cost().isPresent(), "Mission cost wasn't parsed correctly");
        assertEquals(29.75, mission.cost().get(), 0.0001, "Mission cost wasn't parsed correctly");
    }

    @Test
    public void testDeserializeWithOneElementNotGiven() {
        String line =
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,,Success\n";
        Mission mission = parser.deserialize(line);
        assertCommonFields(mission, "0", "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA",
            LocalDate.of(2020, 8, 7),
            "Falcon 9 Block 5", "Starlink V1 L9 & BlackSky", RocketStatus.STATUS_ACTIVE, MissionStatus.SUCCESS);
        assertFalse(mission.cost().isPresent(), "Mission cost wasn't parsed correctly");
    }

    @Test
    public void testDeserializeWithPrelaunchFailure() {
        String line =
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,,Prelaunch Failure\n";
        Mission mission = parser.deserialize(line);
        assertCommonFields(mission, "0", "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA",
            LocalDate.of(2020, 8, 7),
            "Falcon 9 Block 5", "Starlink V1 L9 & BlackSky", RocketStatus.STATUS_ACTIVE, MissionStatus.PRELAUNCH_FAILURE);
        assertFalse(mission.cost().isPresent(), "Mission status wasn't parsed correctly");
    }

    @Test
    public void testDeserializeWithWhiteSpacesInDouble() {
        String line =
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive, 35.60 ,Prelaunch Failure\n";
        Mission mission = parser.deserialize(line);

        assertEquals("0", mission.id());
        assertEquals(MissionStatus.PRELAUNCH_FAILURE, mission.missionStatus());

        assertTrue(mission.cost().isPresent(), "Mission cost with white spaces wasn't parsed correctly");
        assertEquals(35.60, mission.cost().get(), 0.0001);
    }

    @Test
    public void testDeserializeWithThreeFieldsWithQuotes() {
        String line =
            "12,CASC,\"LC-3, Xichang Satellite Launch Center, China\",\"Thu Jul 09, 2020\",Long March 3B/E | Apstar-6D,StatusActive,\"29.15 \",Success\n";
        Mission mission = parser.deserialize(line);

        assertEquals("LC-3, Xichang Satellite Launch Center, China", mission.location());
        assertEquals(LocalDate.of(2020, 7, 9), mission.date());
        assertTrue(mission.cost().isPresent(), "Mission cost with white spaces wasn't parsed correctly");
        assertEquals(29.15, mission.cost().get(), 0.0001);
    }

    @Test
    public void testDeserializeWithNoCost() {
        String line =
            "12,CASC,\"LC-3, Xichang Satellite Launch Center, China\",\"Thu Jul 09, 2020\",Long March 3B/E | Apstar-6D,StatusActive,,Success\n";
        Mission mission = parser.deserialize(line);
        assertFalse(mission.cost().isPresent());
    }

    private void assertCommonFields(Mission mission, String id, String company, String location, LocalDate date,
                                    String rocketName, String payload, RocketStatus rocketStatus,
                                    MissionStatus missionStatus) {
        assertEquals(id, mission.id(), "Mission id wasn't parsed correctly");
        assertEquals(company, mission.company(), "Mission company wasn't parsed correctly");
        assertEquals(location, mission.location(), "Mission location wasn't parsed correctly");
        assertEquals(date, mission.date(), "Mission date wasn't parsed correctly");
        assertEquals(rocketName, mission.detail().rocketName(), "Mission rocketName wasn't parsed correctly");
        assertEquals(payload, mission.detail().payload(), "Mission payload wasn't parsed correctly");
        assertEquals(rocketStatus, mission.rocketStatus(), "Mission rocketStatus wasn't parsed correctly");
        assertEquals(missionStatus, mission.missionStatus(), "Mission status wasn't parsed correctly");
    }
}
