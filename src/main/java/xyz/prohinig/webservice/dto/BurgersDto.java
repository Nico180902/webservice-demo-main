package xyz.prohinig.webservice.dto;

import java.util.List;

public class BurgersDto {

    private final int burgersCount;
    private final List<BurgerDto> burgers;

    public BurgersDto(int burgersCount, List<BurgerDto> burgers) {
        this.burgersCount = burgersCount;
        this.burgers = burgers;
    }

    public int getBurgersCount() {
        return burgersCount;
    }

    public List<BurgerDto> getBurgers() {
        return burgers;
    }
}
