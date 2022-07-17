package javarush.island.items.herbivores;

import javarush.island.datatypes.CreatureType;
import javarush.island.items.Animal;

import java.util.Map;

public class Horse extends Animal implements HerbivoreAnimal {

    public Horse(int maxQuantityInCell, int x, int y, String emoji, double weight, int speed, double howMuchCanEat,
                 Map<CreatureType, Double> probabilityEatCreatures) {
        super(maxQuantityInCell, x, y, emoji, weight, speed, howMuchCanEat, probabilityEatCreatures);
    }
}
