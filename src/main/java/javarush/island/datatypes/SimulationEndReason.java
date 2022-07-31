package javarush.island.datatypes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
//Класс енам - описывает возможные причины остановки симуляции
public enum SimulationEndReason {

    ALL_ANIMALS_DIED("There is no one living animal..."),
    MOVES_ARE_OVER("Simulation moves are over");

    @Getter
    private final String shortDescription;

}