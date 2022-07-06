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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<CartDto> cartDtoList = cartList.stream().map(cartMapper::toCartDto).collect(Collectors.toList());
        return new CartsDto(cartDtoList.size(), cartDtoList);
    }

    @GetMapping("/carts/{cartId}")
    public CartDto getCart(@PathVariable(value = "cartId") int cartId) {

        Cart cart = getCartAndVerifyExists(cartId);

        return cartMapper.toCartDto(cart);
    }

    @DeleteMapping("/carts/{cartId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCart(@PathVariable(value = "cartId") int cartId) {

        Cart cart = getCartAndVerifyExists(cartId);

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

    @PutMapping("carts/{cartId}/burgers")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateCart(@PathVariable(value = "cartId") int cartId, @RequestBody List<BurgerDto> burgerDtoList) {

        Cart cart = getCartAndVerifyExists(cartId);

        List<Burger> burgerList = burgerDtoList.stream().map(burgerMapper::fromBurgerDto).toList();

        if (!cartDAO.deleteAllBurgersOfCart(cart)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        for (Burger burger : burgerList) {
            if (!burgerDAO.persistBurger(burger, cart)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @GetMapping("carts/{cartId}/status")
    public Map<String, Boolean> getCartStatus(@PathVariable(value = "cartId") int cartId) {
        Cart cart = getCartAndVerifyExists(cartId);

        Map<String, Boolean> statusMap = new HashMap<>();
        statusMap.put("active", !cart.isCheckedOut());
        return statusMap;
    }

    @PutMapping("carts/{cartId}/status")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void checkoutCart(@PathVariable(value = "cartId") int cartId, @RequestBody Map<String, Boolean> statusMap) {

        Cart cart = getCartAndVerifyExists(cartId);

        if (cart.isCheckedOut()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This cart is already checked out.");
        }

        if (!statusMap.get("active")) {
            if(!cartDAO.checkoutCart(cart)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (statusMap.get("active")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "It is only allowed to set the active value to 'false'.");
        }

    }

    private Cart getCartAndVerifyExists(int cartId) {

        Cart cart = cartDAO.getCartByID(cartId);

        if (cart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No cart could be found for the ID entered.");
        }

        return cart;
    }
}