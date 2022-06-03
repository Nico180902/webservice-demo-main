package xyz.prohinig.webservice.mapper;

import xyz.prohinig.webservice.dto.CartDto;
import xyz.prohinig.webservice.model.Cart;

public class CartMapper {
    public CartDto toCartDto(Cart cart) {
        return new CartDto(cart.getId(), cart.getBurgers().size(), cart.getTotal(), !cart.isCheckedOut());
    }
}
