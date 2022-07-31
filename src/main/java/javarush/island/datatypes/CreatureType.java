package javarush.island.datatypes;

import javarush.island.items.BasicItem;
import javarush.island.items.Plant;
import javarush.island.items.carnivores.*;
import javarush.island.items.herbivores.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// CreatureType хранит в себе типы сущностей, всех, что могут быть созданы, растение, животные.
// Хранит название для конфига и класс, для создания. В целом создан для удобства ориентации среди типов животных и
// т.д., чтобы можно было их где-то сохранить
@RequiredArgsConstructor
public enum CreatureType {

    PLANT("Plant", Plant.class),

    BEAR("Bear", Bear.class),
    BOA("Boa", Boa.class),
    EAGLE("Eagle", Eagle.class),
    FOX("Fox", Fox.class),
    WOLF("Wolf", Wolf.class),

    BOAR("Boar", Boar.class),
    BUFFALO("Buffalo", Buffalo.class),
    CATERPILLAR("Caterpillar", Caterpillar.class),
    DEER("Deer", Deer.class),
    DUCK("Duck", Duck.class),
    GOAT("Goat", Goat.class),
    HORSE("Horse", Horse.class),
    MOUSE("Mouse", Mouse.class),
    RABBIT("Rabbit", Rabbit.class),
    SHEEP("Sheep", Sheep.class);

    @Getter
    private final String settingsPath;

    @Getter
    private final Class<? extends BasicItem> itemClass;

    public static CreatureType getByItemClass(Class<? extends  BasicItem> itemClass) {
        for (CreatureType creatureType : CreatureType.values()) {
            if(creatureType.getItemClass().equals(itemClass)) return creatureType;
        }
        return null;
    }

    public static CreatureType getBySettingsPath(String settingsPath) {
        for (CreatureType creatureType : CreatureType.values()) {
            if(creatureType.getSettingsPath().equals(settingsPath)) return creatureType;
        }
        return null;
    }
}
