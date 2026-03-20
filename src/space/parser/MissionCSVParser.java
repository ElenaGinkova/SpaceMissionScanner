package space.parser;

import space.mission.Detail;
import space.mission.Mission;
import space.mission.MissionStatus;
import space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class MissionCSVParser implements Parser<Mission> {
    private static final int ID_IDX = 0;
    private static final int COMPANY_IDX = 1;
    private static final int LOCATION_IDX = 2;
    private static final int DATE_IDX = 3;
    private static final int DETAIL_IDX = 4;
    private static final int ROCKET_STATUS_IDX = 5;
    private static final int COST_IDX = 6;
    private static final int MISSION_STATUS_IDX = 7;
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("EEE MMM dd, uuuu", Locale.ENGLISH);

    @Override
    public Mission deserialize(String line) {
        List<String> parsedLine = CSVParserUtils.parseCSVLine(line);
        String id = parsedLine.get(ID_IDX);
        String company = parsedLine.get(COMPANY_IDX);
        String location = parsedLine.get(LOCATION_IDX);
        LocalDate date = parseDate(parsedLine.get(DATE_IDX));
        Detail detail = parseDetail(parsedLine.get(DETAIL_IDX));
        RocketStatus rocketStatus = RocketStatus.stringToEnum(parsedLine.get(ROCKET_STATUS_IDX));
        Optional<Double> cost = CSVParserUtils.parseOptionalDouble(parsedLine.get(COST_IDX).trim());
        MissionStatus missionStatus = MissionStatus.stringToEnum(parsedLine.get(MISSION_STATUS_IDX));

        return new Mission(id, company, location, date, detail, rocketStatus, cost, missionStatus);
    }

    private Detail parseDetail(String col) {
        String[] splitCol = col.split("\\|");
        return new Detail(splitCol[0].trim(), splitCol[1].trim());
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date.trim(), DATE_FORMAT);
    }
}
