package space.rocket;

public enum RocketStatus {
    STATUS_RETIRED("StatusRetired"),
    STATUS_ACTIVE("StatusActive");

    private final String value;

    RocketStatus(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static RocketStatus stringToEnum(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Rocket status is null");
        }
        return switch (value.trim()) {
            case "StatusRetired" -> RocketStatus.STATUS_RETIRED;
            case "StatusActive" -> RocketStatus.STATUS_ACTIVE;
            default -> throw new IllegalArgumentException("Invalid rocket status");
        };
    }
}
