package xyz.prohinig.webservice.mapper;

import xyz.prohinig.webservice.dto.BurgerDto;
import xyz.prohinig.webservice.dto.BurgersDto;
import xyz.prohinig.webservice.dto.IngredientDto;
import xyz.prohinig.webservice.dto.PattyTypeDto;
import xyz.prohinig.webservice.model.*;

import java.util.ArrayList;
import java.util.List;

public class BurgerMapper {

    public BurgerDto toBurgerDto(Burger burger) {

        int burgerId = burger.getId();
        PattyTypeDto pattyTypeDto = toPattyTypeDto(burger.getPattyType());
        List<IngredientDto> ingredientDtoList = toIngredientDto(burger.getCheese(), burger.getSalad(), burger.getTomato());

        return new BurgerDto(burgerId, pattyTypeDto, ingredientDtoList);
    }

    public Burger fromBurgerDto(BurgerDto burgerDto) {

        Integer burgerId = burgerDto.getId();
        PattyType pattyType = PattyType.valueOf(burgerDto.getPattyType().name());
        Ingredient cheese = burgerDto.getIngredients().contains(IngredientDto.CHEESE) ? new Cheese() : null;
        Ingredient salad = burgerDto.getIngredients().contains(IngredientDto.SALAD) ? new Salad() : null;
        Ingredient tomato = burgerDto.getIngredients().contains(IngredientDto.TOMATO) ? new Tomato() : null;

        if(burgerId == null) {
            return new Burger(pattyType, cheese, salad , tomato);
        }

        return new Burger(burgerId, pattyType, cheese, salad , tomato);
    }

    private PattyTypeDto toPattyTypeDto(PattyType pattyType) {
        return PattyTypeDto.valueOf(pattyType.name());
    }

    private List<IngredientDto> toIngredientDto(Ingredient cheese, Ingredient salad, Ingredient tomato) {
        List<IngredientDto> ingredientDtoList = new ArrayList<>();

        if (cheese != null) {
            ingredientDtoList.add(IngredientDto.CHEESE);
        }
        if (salad != null) {
            ingredientDtoList.add(IngredientDto.SALAD);
        }
        if (tomato != null) {
            ingredientDtoList.add(IngredientDto.TOMATO);
        }

        return ingredientDtoList;

    }
}
