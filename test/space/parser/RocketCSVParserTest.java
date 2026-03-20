package space.parser;

import space.rocket.Rocket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RocketCSVParserTest {
    RocketCSVParser parser = new RocketCSVParser();

    @Test
    public void testParseRocketWithAllFields() {
        String line = "3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m";
        Rocket r = parser.deserialize(line);
        assertEquals("3", r.id());
        assertEquals("Unha-3", r.name());
        assertTrue(r.wiki().isPresent());
        assertEquals("https://en.wikipedia.org/wiki/Unha", r.wiki().get());
        assertTrue(r.height().isPresent());
        assertEquals(32.0, r.height().get(), 0.001);
    }

    @Test
    public void testParseRocketWithEmptyHeight() {
        String line = "4,Vanguard,,  m";
        Rocket r = parser.deserialize(line);
        assertFalse(r.height().isPresent());
    }

    @Test
    public void testParseRocketWithWhiteSpacesInHeight() {
        String line = "5,Vanguard,, 22.2 m";
        Rocket r = parser.deserialize(line);
        assertTrue(r.height().isPresent());
        assertEquals(22.2, r.height().get(), 0.001);
    }

    @Test
    public void testParseRocketWithNoHeight() {
        String line = "5,Vanguard,,";
        Rocket r = parser.deserialize(line);
        assertFalse(r.height().isPresent());
    }
}
