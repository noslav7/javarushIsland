package javarush.island.utils;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
//Auxiliary class.

public class MathUtils {

    public <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    public <T> T pickRandomFromList(List<T> inputList) {
        if(inputList.size() == 0) return null;

        return inputList.get(
                ThreadLocalRandom.current().nextInt(0, inputList.size())
        );
    }

}
