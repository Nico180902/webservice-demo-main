package xyz.prohinig.webservice.model;

import static java.util.Objects.requireNonNull;

public class Burger {
    private Integer id = null;
    private final PattyType pattyType;
    private final Ingredient cheese;
    private final Ingredient salad;
    private final Ingredient tomato;

    public Burger(int id, PattyType pattyType, Ingredient cheese, Ingredient salad, Ingredient tomato) {
        this.id = id;
        this.pattyType = requireNonNull(pattyType);
        this.cheese = cheese;
        this.salad = salad;
        this.tomato = tomato;
    }

    public Burger(PattyType pattyType, Ingredient cheese, Ingredient salad, Ingredient tomato) {
        this.pattyType = requireNonNull(pattyType);
        this.cheese = cheese;
        this.salad = salad;
        this.tomato = tomato;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PattyType getPattyType() {
        return pattyType;
    }

    public Ingredient getCheese() {
        return cheese;
    }

    public Ingredient getSalad() {
        return salad;
    }

    public Ingredient getTomato() {
        return tomato;
    }

    public double getPrice() {
        double price = 3;
        price += pattyType.getPrice();
        if (cheese != null) {
            price += cheese.getPrice();
        }
        if (salad != null) {
            price += salad.getPrice();
        }
        if (tomato != null) {
            price += tomato.getPrice();
        }
        return price;
    }

    @Override
    public String toString() {
        return "Burger{" +
                "pattyType=" + pattyType +
                ", cheese=" + cheese +
                ", salad=" + salad +
                ", tomato=" + tomato +
                '}';
    }
}
