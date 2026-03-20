package space.validation;

import space.exception.TimeFrameMismatchException;
import space.mission.Mission;
import space.mission.MissionStatus;

import java.io.OutputStream;
import java.time.LocalDate;

public class SpaceScannerValidations {
    public static void validatePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("From date cannot be before to date");
        }
    }

    public static void validateNumber(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of Missions cannot be less than 1");
        }
    }

    public static boolean validateSuccessfulMissionInInterval(Mission m, LocalDate from, LocalDate to) {
        if (!(m.missionStatus() == MissionStatus.SUCCESS)) {
            return false;
        }
        LocalDate date = m.date();
        return !date.isBefore(from) && !date.isAfter(to);
    }

    public static void validateOutputStream(OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("Output stream cannot be null");
        }
    }
}
