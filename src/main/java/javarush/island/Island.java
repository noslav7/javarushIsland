package javarush.island;

import javarush.island.config.Configuration;
import javarush.island.datatypes.Location;
import javarush.island.datatypes.SimulationEndReason;
import javarush.island.statistic.ImitationStatistic;
import javarush.island.utils.Visualizer;
import lombok.Getter;

//Basic class of the simulation, where the settings, locations and all necessary information can be stored
public class Island {
    @Getter
    private static Island currentSimulation;

    @Getter
    private final Location[][] locations;

    private final ImitationStatistic statistic;

    private final int width, height, statisticsUpdateFrequency;

    private int turnsCount;

    private SimulationEndReason simulationEndReason = null;

    public Island(String settingsFilePath) {

        Configuration configuration = Configuration.getInstance(settingsFilePath);

        statistic = new ImitationStatistic();

        width = configuration.getIslandSetting("width", Integer.class);
        height = configuration.getIslandSetting("height", Integer.class);

        statisticsUpdateFrequency = configuration.getIslandSetting("statisticsUpdateFrequency", Integer.class);

        turnsCount = configuration.getIslandSetting("turnsCount", Integer.class);

        double creatureSpawnChance = configuration.getIslandSetting("creatureSpawnChance", Number.class).doubleValue();

        locations = new Location[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Location location = new Location(configuration, i, j, creatureSpawnChance);

                location.initializeLocation();

                locations[i][j] = location;
            }
        }
    }

    private void startSimulationLoop() {
        while (simulationEndReason == null) {
            if (turnsCount <= 0) {
                simulationEndReason = SimulationEndReason.MOVES_ARE_OVER;

                continue;
            }

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    locations[i][j].doSimulationTick();
                }
            }

            if (statistic.getStatisticValue(ImitationStatistic.EventType.ANIMALS_COUNT) <= 0) {
                simulationEndReason = SimulationEndReason.ALL_ANIMALS_DIED;
                continue;
            }

            Visualizer.visualiseLocationsArray(locations, width, height);
            System.out.println("\n");

            statistic.printStatisticAndClear();

            turnsCount--;

            try {
                Thread.sleep(statisticsUpdateFrequency);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        endSimulation();
    }

    private void endSimulation() {
        System.out.println("\n\n Simulation ended: " + simulationEndReason.getShortDescription() + "\n\n");

        currentSimulation = null;
    }

    public void addStatistic(ImitationStatistic.EventType eventType) {
        statistic.addStatistic(eventType);
    }

    public int getClampHeight() {
        return height - 1;
    }

    public int getClampWidth() {
        return width - 1;
    }

    public Location getLocationOnCoordinates(int x, int y) {
        if ((x >= width || x < 0) || (y >= height || y < 0)) {
            throw new IllegalArgumentException("Invalid X or Y values passed " + x + " " + y);
        }

        return locations[x][y];
    }

    public static void startSimulation(String settingsFilePath) {
        if (currentSimulation != null) {
            throw new IllegalStateException("Simulation already started!");
        }

        currentSimulation = new Island(settingsFilePath);

        currentSimulation.startSimulationLoop();
    }
}
