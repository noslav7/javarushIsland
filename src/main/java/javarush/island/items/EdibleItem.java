package javarush.island.items;

import lombok.Getter;

/**
 * Abstraction provides an opportunity to eat an object
 */
public abstract class EdibleItem extends BasicItem {
    @Getter
    private double weight;

    public EdibleItem(int maxQuantityInCell, int x, int y, String emoji, double weight) {
        super(maxQuantityInCell, x, y, emoji);

        if(weight <= 0) {
            throw new IllegalArgumentException("Item weight cannot be negative or zero");
        }

        this.weight = weight;
    }

    public void setWeight(double weight) {
        if(weight <= 0) weight = -1;

        this.weight = weight;
    }

    public boolean isDeath() {
        return weight == -1;
    }
}
