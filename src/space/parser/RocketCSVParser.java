package space.parser;

import space.rocket.Rocket;

import java.util.List;
import java.util.Optional;

public class RocketCSVParser implements Parser<Rocket> {
    private static final int ID_IDX = 0;
    private static final int NAME_IDX = 1;
    private static final int WIKI_IDX = 2;
    private static final int HEIGHT_IDX = 3;

    @Override
    public Rocket deserialize(String line) {
        List<String> parsedLine = CSVParserUtils.parseCSVLine(line);
        String id = parsedLine.get(ID_IDX);
        String name = parsedLine.get(NAME_IDX);
        Optional<String> wiki =
            parsedLine.get(WIKI_IDX).isEmpty() ? Optional.empty() : Optional.of(parsedLine.get(WIKI_IDX));
        Optional<Double> height = parseHeight(parsedLine.get(HEIGHT_IDX));
        return new Rocket(id, name, wiki, height);
    }

    private Optional<Double> parseHeight(String col) {
        if (col.isEmpty()) {
            return Optional.empty();
        }
        col = col.replace("m", "");
        return CSVParserUtils.parseOptionalDouble(col);
    }
}
