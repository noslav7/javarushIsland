package javarush.island.items;

import javarush.island.Island;
import javarush.island.config.Configuration;
import javarush.island.datatypes.CreatureType;
import javarush.island.datatypes.Direction;
import javarush.island.datatypes.ITicking;
import javarush.island.datatypes.Location;
import javarush.island.statistic.EventType;
import javarush.island.utils.MathUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@ToString
public abstract class Animal extends EdibleItem implements ITicking, Cloneable {

    private final int speed;
    private final double howMuchCanEat;

    //Map with fractional chances of eat other creatures
    private final Map<CreatureType, Double> probabilityEatCreatures;

    private double foodSaturation;
    private final double saturationReductionRate;

    private Direction direction = null;

    public Animal(int maxQuantityInCell, int x, int y, String emoji, double weight, int speed,
                  double howMuchCanEat, Map<CreatureType, Double> probabilityEatCreatures) {
        super(maxQuantityInCell, x, y, emoji, weight);

        this.speed = speed;
        this.howMuchCanEat = howMuchCanEat;
        this.probabilityEatCreatures = probabilityEatCreatures;

        this.foodSaturation = getWeight();
        this.saturationReductionRate = howMuchCanEat / getWeight();
    }
}