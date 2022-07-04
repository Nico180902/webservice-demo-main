package xyz.prohinig.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import xyz.prohinig.webservice.database.CartDAO;
import xyz.prohinig.webservice.dto.CartDto;
import xyz.prohinig.webservice.dto.CartsDto;
import xyz.prohinig.webservice.mapper.CartMapper;
import xyz.prohinig.webservice.model.Cart;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CartController {

    private final CartDAO cartDAO;
    private final CartMapper cartMapper;

    @Autowired
    public CartController(CartDAO cartDAO, CartMapper cartMapper) {
        this.cartDAO = cartDAO;
        this.cartMapper = cartMapper;
    }


    @GetMapping("/carts")
    public CartsDto getAllCarts() {
        List<Cart> cartList = cartDAO.getAllCarts();
        List<CartDto> cartDtoList = cartList.stream()
                .map(cartMapper::toCartDto)
                .collect(Collectors.toList());
        return new CartsDto(cartDtoList.size(), cartDtoList);
    }

    @GetMapping("/carts/{cartId}")
    public CartDto getCart(@PathVariable(value = "cartId") int cartId) {
        Cart cart = cartDAO.getCartByID(cartId);
        if (cart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No cart could be found for the ID entered.");
        }

        return cartMapper.toCartDto(cart);
    }

    @DeleteMapping("/carts/{cartId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCart(@PathVariable(value = "cartId") int cartId) {
        Cart cart = cartDAO.getCartByID(cartId);

        if(cart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No cart could be found for the ID entered.");
        }

        if (!cartDAO.deleteCart(cart)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}