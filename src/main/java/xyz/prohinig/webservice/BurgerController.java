package xyz.prohinig.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import xyz.prohinig.webservice.database.BurgerDAO;
import xyz.prohinig.webservice.database.CartDAO;
import xyz.prohinig.webservice.dto.BurgerDto;
import xyz.prohinig.webservice.dto.BurgersDto;
import xyz.prohinig.webservice.mapper.BurgerMapper;
import xyz.prohinig.webservice.model.Burger;
import xyz.prohinig.webservice.model.Cart;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BurgerController {

    private final BurgerDAO burgerDAO;

    private final CartDAO cartDAO;
    private final BurgerMapper burgerMapper;

    @Autowired
    public BurgerController(BurgerDAO burgerDAO, BurgerMapper burgerMapper, CartDAO cartDAO) {
        this.burgerDAO = burgerDAO;
        this.burgerMapper = burgerMapper;
        this.cartDAO = cartDAO;
    }


    @GetMapping("/carts/{cartId}/burgers")
    public BurgersDto getBurgersOfCart(@PathVariable(value = "cartId") int cartId) {

        Cart cart = getCartAndVerifyExists(cartId);

        List<Burger> burgerList = burgerDAO.getBurgersOfCart(cart);

        List<BurgerDto> burgerDtoList = burgerList.stream()
                .map(burgerMapper::toBurgerDto)
                .collect(Collectors.toList());

        return new BurgersDto(burgerDtoList.size(), burgerDtoList);

    }

    @GetMapping("/carts/{cartId}/burgers/{burgerId}")
    public BurgerDto getSpecificBurgerOfCart(@PathVariable(value = "cartId") int cartId, @PathVariable(value = "burgerId") int burgerId ) {

        Cart cart = getCartAndVerifyExists(cartId);

        BurgerDto burgerDto = cart.getBurgers().stream()
                .filter(burger -> burger.getId().equals(burgerId))
                .map(burgerMapper::toBurgerDto)
                .findFirst()
                .orElse(null);

        if(burgerDto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return burgerDto;

    }

    @PostMapping("/carts/{cartId}/burgers")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void addBurger(@PathVariable(value = "cartId") int cartId, @RequestBody BurgerDto burgerDto) {

        Cart cart = getCartAndVerifyExists(cartId);

        Burger burger = burgerMapper.fromBurgerDto(burgerDto);

        if (!burgerDAO.persistBurger(burger, cart)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    private Cart getCartAndVerifyExists(int cartId) {

        Cart cart = cartDAO.getCartByID(cartId);

        if(cart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }


        return cart;
    }
}
