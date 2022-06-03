package xyz.prohinig.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.prohinig.webservice.database.BurgerDAO;
import xyz.prohinig.webservice.database.CartDAO;
import xyz.prohinig.webservice.database.DatabaseConnection;
import xyz.prohinig.webservice.mapper.BurgerMapper;
import xyz.prohinig.webservice.mapper.CartMapper;

@Configuration
public class AppConfiguration {

    @Bean
    public DatabaseConnection databaseConnection() {
        return new DatabaseConnection();
    }

    @Bean
    public CartDAO cartDAO(@Autowired DatabaseConnection databaseConnection) {
        return new CartDAO(databaseConnection);
    }

    @Bean
    public CartMapper cartMapper() {
        return new CartMapper();
    }

    @Bean
    public BurgerDAO burgerDAO(@Autowired DatabaseConnection databaseConnection) {
        return new BurgerDAO(databaseConnection);
    }

    @Bean
    public BurgerMapper burgerMapper() {
        return new BurgerMapper();
    }
}
