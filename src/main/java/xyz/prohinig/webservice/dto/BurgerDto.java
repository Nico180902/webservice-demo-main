package xyz.prohinig.webservice.dto;

import xyz.prohinig.webservice.model.Ingredient;
import xyz.prohinig.webservice.model.PattyType;

import java.util.List;

public class BurgerDto {

    private final Integer id;
    private final PattyTypeDto pattyType;
    private final List<IngredientDto> ingredients;

    public BurgerDto(Integer id, PattyTypeDto pattyType, List<IngredientDto> ingredients) {
        this.id = id;
        this.pattyType = pattyType;
        this.ingredients = ingredients;
    }

    public Integer getId() {
        return id;
    }

    public PattyTypeDto getPattyType() {
        return pattyType;
    }

    public List<IngredientDto> getIngredients() {
        return ingredients;
    }
}
