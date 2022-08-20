package javarush.island.statistic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class ImitationStatistic {
    /**
     * The class to calculate statistics
     */

    private final Map<EventType, Integer> statistic = new HashMap<>();

    public void addStatistic(EventType eventType) {
        statistic.put(
                eventType, statistic.getOrDefault(eventType, 0) + 1
        );
    }

    public int getStatisticValue(EventType eventType) {
        return statistic.getOrDefault(eventType, 0);
    }

    public void printStatisticAndClear() {
        StringBuilder stringBuilder = new StringBuilder();

        for (EventType eventType : statistic.keySet()) {
            stringBuilder.append(eventType.getDescription()).append(": ").append(statistic.get(eventType)).append("\n");
        }

        System.out.println(stringBuilder);

        statistic.clear();
    }

    @RequiredArgsConstructor
    @Getter
    public enum EventType {

        ANIMALS_COUNT("General animals count"),
        MOVE("Animals, who changed their location"),
        REPRODUCE("Animals, who gave birth to a child"),
        EAT("Animals, who eat");

        private final String description;

    }
}


