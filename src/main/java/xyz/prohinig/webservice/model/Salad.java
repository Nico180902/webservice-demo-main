package xyz.prohinig.webservice.model;

public class Salad implements Ingredient {
    @Override
    public double getPrice() {
        return 1;
    }

    @Override
    public String toString() {
        return "Salad{}";
    }
}
