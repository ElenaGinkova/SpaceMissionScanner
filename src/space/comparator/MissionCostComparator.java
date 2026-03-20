package space.comparator;

import space.mission.Mission;

import java.util.Comparator;

public class MissionCostComparator implements Comparator<Mission> {
    @Override
    public int compare(Mission o1, Mission o2) {
        return Double.compare(o1.cost().orElseThrow(), o2.cost().orElseThrow());
    }
}
