package xyz.prohinig.webservice.dto;

public class CartDto {
    private final int id;
    private final int burgerCount;
    private final double total;
    private final boolean active;

    public CartDto(int id, int burgerCount, double total, boolean active) {
        this.id = id;
        this.burgerCount = burgerCount;
        this.total = total;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public int getBurgerCount() {
        return burgerCount;
    }

    public double getTotal() {
        return total;
    }

    public boolean isActive() {
        return active;
    }
}
