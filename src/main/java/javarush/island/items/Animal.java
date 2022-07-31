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
     * Абстрактный класс для любого животного.
     * Наследует EdibleItem, поскольку может быть съеден (имеет свой вес).
     * Наследует ITicking, т.к. обновляется каждый шаг симуляции.
     * Наследует Cloneable чтобы можно было клонировать
     */


    //Настройки животного.
    private final int speed;
    private final double howMuchCanEat;

    //Map with fractional chances of eat other creatures
    private final Map<CreatureType, Double> probabilityEatCreatures;

    //Насыщенность (голод) животного
    private double foodSaturation;

    //Специально высчитанный параметр, на сколько голод уменьшается каждый ход симуляции
    private final double saturationReductionRate;

    //Временное направление движения
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

    /**
     * @param edibleItem Что можно съесть
     * @return Вернёт десятичный шанс съесть другую сущность (Десятичный шанс: 0 <= chance <= 1)
     */
    public double getProbabilityEatAnother(EdibleItem edibleItem) {
        CreatureType creatureType = edibleItem.getCreatureType();

        if(creatureType == null) return 0;

        return probabilityEatCreatures.getOrDefault(creatureType, 0.0);
    }

    @Override
    public void doSimulationTick() {
        //Каждый тик симуляции вызывается данный метод

        //Получаем локацию, на которой стоит животное, на данный момент
        Location currentLocation = Island.getCurrentSimulation().getLocationOnCoordinates(this.getX(), this.getY());

        //Если попали в шанс - кушаем
        if(ThreadLocalRandom.current().nextBoolean()) eat(currentLocation);

        //После отнимаем голод и проверяем, не упал ли он ниже нуля
        this.foodSaturation -= saturationReductionRate;
        if(foodSaturation <= 0) {
            //Если голод <= 0  -> сущность умерла, удаляем с текущей локации
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


    /**
     * Creates a new animal (Class specified by enum). With reflection utils and config settings
     * @param creatureType Type of creature
     * @param configuration Configuration instance (To prevent permanent calls to .getConfiguration())
     * @return New Animal with config settings
     */
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