package xyz.prohinig.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import xyz.prohinig.webservice.database.BurgerDAO;
import xyz.prohinig.webservice.database.CartDAO;
import xyz.prohinig.webservice.dto.BurgerDto;
import xyz.prohinig.webservice.dto.CartDto;
import xyz.prohinig.webservice.dto.CartsDto;
import xyz.prohinig.webservice.mapper.BurgerMapper;
import xyz.prohinig.webservice.mapper.CartMapper;
import xyz.prohinig.webservice.model.Burger;
import xyz.prohinig.webservice.model.Cart;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CartController {

    private final CartDAO cartDAO;
    private final BurgerDAO burgerDAO;
    private final CartMapper cartMapper;
    private final BurgerMapper burgerMapper;

    @Autowired
    public CartController(CartDAO cartDAO, BurgerDAO burgerDAO, CartMapper cartMapper, BurgerMapper burgerMapper) {
        this.cartDAO = cartDAO;
        this.burgerDAO = burgerDAO;
        this.cartMapper = cartMapper;
        this.burgerMapper = burgerMapper;
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

        if (cart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No cart could be found for the ID entered.");
        }

        if (!cartDAO.deleteCart(cart)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/carts")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void addCart(@RequestBody List<BurgerDto> burgerDtosList) {

        Cart cart = cartDAO.createEmptyCart();

        if (cart == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<Burger> burgerList = burgerDtosList.stream().map(burgerMapper::fromBurgerDto).toList();

        burgerList.forEach(cart::addBurger);

        for (Burger burger : cart.getBurgers()) {
            if (!burgerDAO.persistBurger(burger, cart)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    }
}