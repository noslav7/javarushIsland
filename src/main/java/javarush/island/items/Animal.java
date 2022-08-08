package javarush.island.items;

import javarush.island.Island;
import javarush.island.config.Configuration;
import javarush.island.datatypes.CreatureType;
import javarush.island.datatypes.Direction;
import javarush.island.datatypes.ITicking;
import javarush.island.datatypes.Location;
import javarush.island.statistic.ImitationStatistic;
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
    /**
     * The abstract class for any animal.
     * The class extends EdibleItem, because the former can be eaten  (has it's weight).
     * Also, the class extends ITicking, because it does something every simulation act.
     * And extends Cloneable, for making it possible to be cloned
     */

    private final int speed;
    private final double howMuchCanEat;

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

    public double getProbabilityEatAnother(EdibleItem edibleItem) {
        CreatureType creatureType = edibleItem.getCreatureType();

        if(creatureType == null) return 0;

        return probabilityEatCreatures.getOrDefault(creatureType, 0.0);
    }

    @Override
    public void doSimulationTick() {

        Location currentLocation = Island.getCurrentSimulation().getLocationOnCoordinates(this.getX(), this.getY());

        if(ThreadLocalRandom.current().nextBoolean()) eat(currentLocation);

        this.foodSaturation -= saturationReductionRate;
        if(foodSaturation <= 0) {
            this.setWeight(-1);
            currentLocation.removeItem(this);
            return;
        }

        Island.getCurrentSimulation().addStatistic(ImitationStatistic.EventType.ANIMALS_COUNT);

        if(ThreadLocalRandom.current().nextBoolean() &&
                currentLocation.getCountOfCreatures(getCreatureType()) < getMaxQuantityInCell())
            reproduce(currentLocation);

        if(ThreadLocalRandom.current().nextBoolean()) move(currentLocation);
    }

    public void eat(Location currentLocation) {
        BasicItem basicItem = MathUtils.pickRandomFromList(currentLocation.getLocationItems(this));
        if(basicItem == null) return;

        if(!EdibleItem.class.isAssignableFrom(basicItem.getClass())) return;

        EdibleItem edibleItem = (EdibleItem) basicItem;

        boolean victimPlant = Plant.class.isAssignableFrom(basicItem.getClass());
        if(!victimPlant && edibleItem.getWeight() > howMuchCanEat) return;

        if(Math.random() > getProbabilityEatAnother(edibleItem)) return;

        double requiredSaturation = getWeight() - foodSaturation;
        if(victimPlant && edibleItem.getWeight() > requiredSaturation) {
            this.updateFoodSaturationFactor(requiredSaturation);

            edibleItem.setWeight(edibleItem.getWeight() - requiredSaturation);
        } else {
            this.updateFoodSaturationFactor(edibleItem.getWeight());

            edibleItem.setWeight(-1);
        }

        if(edibleItem.isDeath()) currentLocation.removeItem(edibleItem);

        Island.getCurrentSimulation().addStatistic(ImitationStatistic.EventType.EAT);
    }

    private void updateFoodSaturationFactor(double victimWeight) {
        if(foodSaturation + victimWeight > getWeight()) foodSaturation = getWeight();
        else foodSaturation += victimWeight;
    }

    public void move(Location currentLocation) {
        this.direction = Direction.getRandomNonEmpty();

        moveInCurrentDirection(currentLocation);
    }

    private void moveInCurrentDirection(Location currentLocation) {
        if(direction == null) {
            throw new NullPointerException("Trying to move in null direction!");
        }

        if(direction.equals(Direction.EMPTY)) return;

        int resultX = getX(), resultY = getY();

        switch (direction) {
            case UP: {
                resultY = MathUtils.clamp(getY() + speed, 0, Island.getCurrentSimulation().getClampHeight());
                break;
            }
            case DOWN: {
                resultY = MathUtils.clamp(getY() - speed, 0, Island.getCurrentSimulation().getClampHeight());
                break;
            }

            case RIGHT: {
                resultX = MathUtils.clamp(getX() + speed, 0, Island.getCurrentSimulation().getClampWidth());
                break;
            }

            case LEFT: {
                resultX = MathUtils.clamp(getX() - speed, 0, Island.getCurrentSimulation().getClampWidth());
                break;
            }

            default:
                return;
        }

        Location resultLocation = Island.getCurrentSimulation().getLocationOnCoordinates(resultX, resultY);

        if(resultLocation.equals(currentLocation)) return;

        boolean successful = resultLocation.addNewBasicItem(this);

        if(!successful) return;

        this.setX(resultX);
        this.setY(resultY);

        currentLocation.removeItem(this);

        Island.getCurrentSimulation().addStatistic(ImitationStatistic.EventType.MOVE);
    }

    public void reproduce(Location currentLocation) {
        Animal child = this.clone();

        child.setFoodSaturation(child.getWeight());
        child.setDirection(null);

        currentLocation.addChild(child);

        Island.getCurrentSimulation().addStatistic(ImitationStatistic.EventType.REPRODUCE);
    }

    public static Animal createNewAnimal(CreatureType creatureType, Configuration configuration,
                                         int xPosition, int yPosition) {
        try {
            Constructor<? extends BasicItem> constructor =
                    creatureType.getItemClass().getDeclaredConstructor(Animal.class.getConstructors()[0].getParameterTypes());

            return (Animal) constructor.newInstance(
                    configuration.getCreatureSetting(creatureType, "maxNumberPerOneLocation", Number.class).intValue(),
                    xPosition, yPosition,
                    configuration.getCreatureSetting(creatureType, "emoji", String.class),
                    configuration.getCreatureSetting(creatureType, "weight", Number.class).doubleValue(),
                    configuration.getCreatureSetting(creatureType, "movementSpeed", Number.class).intValue(),
                    configuration.getCreatureSetting(creatureType, "weightFoodCanEat", Number.class).doubleValue(),
                    configuration.getProbabilityEatCreatures(creatureType)
            );
        }catch (NoSuchMethodException exception) {
            System.err.println("Trying to create invalid animal ".concat(creatureType.name()));
        }catch (IllegalArgumentException exception) {
            System.err.println("Trying to create animal with invalid arguments ".concat(creatureType.name()));
        }catch (Exception exception) {
            exception.printStackTrace();

            System.err.println("Exception when use reflection for: ".concat(creatureType.name()));
        }

        return null;
    }

    @Override
    public Animal clone() {
        try {
            return (Animal) super.clone();
        }catch (CloneNotSupportedException exception) {
            exception.printStackTrace();

            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Animal animal = (Animal) o;

        return getX() == animal.getX() && getY() == animal.getY();
    }
}