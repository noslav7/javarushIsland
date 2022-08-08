package javarush.island.utils;

import javarush.island.datatypes.Location;
import javarush.island.items.BasicItem;
import javarush.island.items.Plant;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
//Auxiliary class, similar to MathUtils
//Created for current island conditions display

public class Visualizer {

    public void visualiseLocationsArray(Location[][] locations, int maxWidth, int maxHeight) {
        System.out.println("Visualizing: X:Y   animals");

        StringBuilder stringBuilder = new StringBuilder();
        for (int x = 0; x < maxWidth; x++) {
            for (int y = 0; y < maxHeight; y++) {
                Location location = locations[x][y];

                Map<String, Double> visualize = new HashMap<>();
                location.getLocationItems(null)
                        .forEach(basicItem -> {
                            String emoji = basicItem.getEmoji();
                            visualize.put(emoji, visualize.getOrDefault(emoji, 0.0) + visualizeBasicItemCount(basicItem));
                        });

                stringBuilder.append(x).append(":").append(y)
                        .append("\t").append(mapToString(visualize)).append("\n");
            }
        }

        System.out.print("\n" + stringBuilder);

    }

    private double visualizeBasicItemCount(BasicItem basicItem) {
        if(Plant.class.isAssignableFrom(basicItem.getClass())) {
            return ((Plant) basicItem).getWeight();
        }

        return 1;
    }

    private StringBuilder mapToString(Map<String, Double> inputMap) {
        StringBuilder builder = new StringBuilder();

        for(String key : inputMap.keySet()) {
            builder.append(key).append("x").append(inputMap.get(key).intValue()).append("   ");
        }

        return builder;
    }

}
