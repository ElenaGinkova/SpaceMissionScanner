package space.mission;

public enum MissionStatus {
    SUCCESS("Success"),
    FAILURE("Failure"),
    PARTIAL_FAILURE("Partial Failure"),
    PRELAUNCH_FAILURE("Prelaunch Failure");

    private final String value;

    MissionStatus(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static MissionStatus stringToEnum(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Mission status is null");
        }
        return switch (value.trim()) {
            case "Success" -> MissionStatus.SUCCESS;
            case "Failure" -> MissionStatus.FAILURE;
            case "Partial Failure" -> MissionStatus.PARTIAL_FAILURE;
            case "Prelaunch Failure" -> MissionStatus.PRELAUNCH_FAILURE;
            default -> throw new IllegalArgumentException("Invalid mission status");
        };
    }
}
