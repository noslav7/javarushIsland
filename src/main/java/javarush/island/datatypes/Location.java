package javarush.island.datatypes;

import javarush.island.config.Configuration;
import javarush.island.items.Animal;
import javarush.island.items.BasicItem;
import javarush.island.items.Plant;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


//Location class. It stores all information about location, creatures in it, movement among locations
@RequiredArgsConstructor
public class Location {

    private final Configuration configuration;

    private final int xCoordinate;

    private final int yCoordinate;

    private final double creatureSpawnChance;

    private final int creaturesCountDivider = 5;

    private final List<BasicItem> locationItems = new ArrayList<>();

    private final List<BasicItem> listOfDeletedOrMoved = new ArrayList<>();

    private final List<Animal> listOfCreatedChildren = new ArrayList<>();

    private boolean inSimulationTick = false;

    public void initializeLocation() {
        growPlants();

        fillAnimals();
    }

    public void doSimulationTick() {
        inSimulationTick = true;

        for (BasicItem basicItem : locationItems) {
            if (ITicking.class.isAssignableFrom(basicItem.getClass())) {
                ((ITicking) basicItem).doSimulationTick();
            }
        }

        locationItems.removeAll(listOfDeletedOrMoved);
        listOfDeletedOrMoved.clear();

        locationItems.addAll(listOfCreatedChildren);
        listOfCreatedChildren.clear();

        inSimulationTick = false;
    }

    private void growPlants() {
        if (!ThreadLocalRandom.current().nextBoolean()) return;

        int randomPlantWeight = ThreadLocalRandom.current().nextInt(0,
                configuration.getCreatureSetting(CreatureType.PLANT, "maxNumberPerOneLocation", Number.class).intValue());

        if (randomPlantWeight == 0) return;

        locationItems.add(new Plant(
                xCoordinate, yCoordinate, configuration.getCreatureSetting(CreatureType.PLANT, "emoji", String.class),
                randomPlantWeight
        ));
    }

    private void fillAnimals() {
        for (CreatureType creatureType : CreatureType.values()) {
            if (!Animal.class.isAssignableFrom(creatureType.getItemClass())) continue;
            if (ThreadLocalRandom.current().nextDouble() > creatureSpawnChance) continue;

            int maxQuantityInCell = configuration.getCreatureSetting(creatureType, "maxNumberPerOneLocation", Number.class)
                    .intValue() / creaturesCountDivider;

            if (maxQuantityInCell == 0) continue;
            int animalsCount = ThreadLocalRandom.current().nextInt(0, maxQuantityInCell);
            if (animalsCount == 0) continue;

            Animal newAnimal = Animal.createNewAnimal(creatureType, configuration, xCoordinate, yCoordinate);

            if (newAnimal == null) {
                System.err.println("Unknown error while creating new Animal " + this);

                continue;
            }

            for (int i = 0; i < animalsCount; i++) locationItems.add(newAnimal.clone());
        }
    }

    public void removeItem(BasicItem basicItem) {
        if (inSimulationTick) {
            listOfDeletedOrMoved.add(basicItem);
        } else {
            locationItems.remove(basicItem);
        }
    }

    public boolean addNewBasicItem(BasicItem basicItem) {
        if (getCountOfCreatures(basicItem.getCreatureType()) >= basicItem.getMaxQuantityInCell()) {
            return false;
        }

        if (inSimulationTick) {
            System.err.println("[LOG] An attempt to update location while location modifying");
            return false;
        }

        locationItems.add(basicItem);

        return true;
    }

    public void addChild(Animal animal) {
        listOfCreatedChildren.add(animal);
    }

    public long getCountOfCreatures(CreatureType creatureType) {
        return getLocationItems(null)
                .stream()
                .filter(basicItem -> basicItem.getCreatureType().equals(creatureType))
                .count();
    }

    public List<BasicItem> getLocationItems(BasicItem basicItem) {
        List<BasicItem> basicItems = new ArrayList<>(locationItems);
        basicItems.removeAll(listOfDeletedOrMoved);
        basicItems.addAll(listOfCreatedChildren);

        if (basicItem != null) basicItems.remove(basicItem);

        return basicItems;
    }

    public long getAnimalsCount() {
        return getLocationItems(null)
                .stream()
                .filter(basicItem -> !basicItem.getCreatureType().equals(CreatureType.PLANT))
                .count();
    }
}
