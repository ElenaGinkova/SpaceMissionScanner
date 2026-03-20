package space.parser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVParserUtilsTest {

    @Test
    public void testParseCSVLine() {
        String line = "1col,2col,  3col";
        List<String> parsedCols = CSVParserUtils.parseCSVLine(line);
        List<String> expectedCols = new ArrayList<>();
        expectedCols.add("1col");
        expectedCols.add("2col");
        expectedCols.add("3col");
        assertEquals(expectedCols, parsedCols);
    }

    @Test
    public void testParseOptionalDouble() {
        assertEquals(Optional.empty(), CSVParserUtils.parseOptionalDouble(null));
    }

    @Test
    public void testParseCountryFromLocation() {
        assertEquals("USA", CSVParserUtils.parseCountryFromLocation("SLC-41, Cape Canaveral AFS, Florida, USA"));
    }

    @Test
    public void testParseCountryFromLocationEmpty() {
        assertEquals("", CSVParserUtils.parseCountryFromLocation(""));
    }

    @Test
    public void testParseCountryFromLocationWithOnlyCountry() {
        assertEquals("USA", CSVParserUtils.parseCountryFromLocation("USA"));
    }
}