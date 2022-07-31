package javarush.island.config;


import javarush.island.datatypes.CreatureType;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Configuration {
    /**
     * Класс описывающий конфиг и созданный для взаимодействия с ним, хранит все настройки
     */

    private static Configuration INSTANCE;

    private final Map<String, Object> creaturesSettings = new HashMap<>();

    private final Map<String, Object> islandSettings = new HashMap<>();

    @SuppressWarnings("unchecked")
    private Configuration(String settingsFilePath) {
        Yaml yaml = new Yaml();

        Map<String, Object> allSettings = null;
        try {
            if(settingsFilePath == null) {
                allSettings = yaml.load(getClass().getClassLoader().getResourceAsStream("creatures.yml"));
            }else {
                File file = new File(settingsFilePath);

                allSettings = yaml.load(
                        new FileInputStream(file)
                );
            }
        } catch (Exception exception) {
            exception.printStackTrace();

            System.err.println("File with creatures settings does not found!");
            System.exit(1);
        }

        if(allSettings == null) {
            throw new NullPointerException("Settings not loaded");
        }

        if(!allSettings.containsKey("IslandSettings") || !allSettings.containsKey("Creatures")) {
            System.err.println("Main settings does not found in settings-file!");
            System.exit(1);
        }

        islandSettings.putAll(((Map<String, Object>) allSettings.get("IslandSettings")));
        creaturesSettings.putAll(((Map<String, Object>) allSettings.get("Creatures")));
    }

    public static Configuration getInstance(String settingsFilePath) {
        if (INSTANCE == null) {
            INSTANCE = new Configuration(settingsFilePath);
        }
        return INSTANCE;
    }

    //Обобщённый метод для получения настроек острова с возможностью скастить сразу
    public <T> T getIslandSetting(String settingName, Class<T> castTo) {
        Object setting = islandSettings.getOrDefault(settingName, null);

        if(setting == null) {
            throw new NullPointerException("Parameter named ".concat(settingName).concat(" not found"));
        }

        return applyGeneric(setting, castTo);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCreatureSetting(CreatureType creatureType, String settingName, Class<T> castTo) {
        Map<String, Object> creatureSettings = (Map<String, Object>) creaturesSettings.get(creatureType.getSettingsPath());

        Object setting = creatureSettings.getOrDefault(settingName, null);

        if(setting == null) {
            throw new NullPointerException("Parameter named ".concat(settingName).concat(" not found").concat(" ")
                    .concat(creatureType.name()));
        }

        return applyGeneric(setting, castTo);
    }

    //Обобщённый метод для превращения Object в <T>
    private <T> T applyGeneric(Object setting, Class<T> castTo) {
        if(!castTo.isAssignableFrom(setting.getClass())) {
            throw new IllegalArgumentException("Trying to cast to the wrong type: "
                    .concat(setting.getClass().getName())
                    .concat(" => ")
                    .concat(castTo.getName())
            );
        }

        return castTo.cast(setting);
    }

    //Метод для получения шансов на съедение других сущностей
    public Map<CreatureType, Double> getProbabilityEatCreatures(CreatureType creatureType) {
        Map<String, Object> rawConfigMap = getCreatureSetting(creatureType, "chanceToEat", Map.class);

        Map<CreatureType, Double> resultMap = new HashMap<>();
        for(String creatureKey : rawConfigMap.keySet()) {
            CreatureType currentCreatureType = CreatureType.getBySettingsPath(creatureKey);

            if(currentCreatureType == null) {
                throw new NullPointerException("The Creature with name ".concat(creatureKey)
                        .concat(" not found in chances to eat of ").concat(creatureType.name()));
            }

            resultMap.put(currentCreatureType, ((Number) rawConfigMap.get(creatureKey)).doubleValue() / 100);
        }

        return resultMap;
    }
}

