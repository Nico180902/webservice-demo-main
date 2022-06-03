package xyz.prohinig.webservice.model;

public class Tomato implements Ingredient {
    @Override
    public double getPrice() {
        return 0.5;
    }

    @Override
    public String toString() {
        return "Tomato{}";
    }
}
