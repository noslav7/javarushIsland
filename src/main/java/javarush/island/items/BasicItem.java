package javarush.island.items;

import javarush.island.Island;
import javarush.island.datatypes.CreatureType;
import javarush.island.utils.MathUtils;
import lombok.Getter;

@Getter
public abstract class BasicItem {
    /**
     * Basic item in cell
     */

    private final int maxQuantityInCell;

    private int x;
    private int y;

    private final String emoji;

    private final CreatureType creatureType;

    public BasicItem(int maxQuantityInCell, int x, int y, String emoji) {
        this.maxQuantityInCell = maxQuantityInCell;
        this.x = x;
        this.y = y;
        this.emoji = emoji;

        this.creatureType = CreatureType.getByItemClass(getClass());
    }

    public void setX(int x) {
        this.x = MathUtils.clamp(x, 0, Island.getCurrentSimulation().getClampWidth());
    }

    public void setY(int y) {
        this.y = MathUtils.clamp(y, 0, Island.getCurrentSimulation().getClampHeight());
    }
}
