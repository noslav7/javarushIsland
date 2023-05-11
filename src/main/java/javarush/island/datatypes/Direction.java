package javarush.island.datatypes;

import javarush.island.utils.MathUtils;

import java.util.Arrays;
import java.util.List;

//The class describes all possible movement directions
public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,

    EMPTY;

    private static final List<Direction> nonEmptyDirections =
            Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);

    public static Direction getRandomNonEmpty() {
        return MathUtils.pickRandomFromList(nonEmptyDirections);
    }
}
