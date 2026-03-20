package space.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CSVParserUtils {
    private CSVParserUtils() {
    }

    public static List<String> parseCSVLine(String line) {
        List<String> parsedCols = new ArrayList<>();
        StringBuilder currCol = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && c == ',') {
                parsedCols.add(currCol.toString().trim());
                currCol.setLength(0);
            } else {
                currCol.append(c);
            }
        }

        parsedCols.add(currCol.toString().trim());
        return parsedCols;
    }

    public static Optional<Double> parseOptionalDouble(String val) {
        if (val == null) {
            return Optional.empty();
        }
        val = val.trim();
        if (val.isEmpty()) {
            return Optional.empty();
        }
        val = val.replace(",", "");
        return val.isEmpty() ? Optional.empty() : Optional.of(Double.parseDouble(val));
    }

    public static String parseCountryFromLocation(String location) {
        if (location.isEmpty()) {
            return "";
        }
        int lastIdx = location.lastIndexOf(',');
        if (lastIdx == -1) {
            return location;
        }
        return location.substring(lastIdx + 1).trim();
    }
}
