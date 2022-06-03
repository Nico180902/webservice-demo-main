package xyz.prohinig.webservice.dto;

import java.util.List;

public class CartsDto {
    private final int totalCount;
    private final List<CartDto> carts;

    public CartsDto(int totalCount, List<CartDto> carts) {
        this.totalCount = totalCount;
        this.carts = carts;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<CartDto> getCarts() {
        return carts;
    }
}
