package xyz.prohinig.webservice.database;

import xyz.prohinig.webservice.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BurgerDAO {

    private static final String BURGER_ID_COLUMN = "id";
    private static final String BURGER_PATTY_TYPE_COLUMN = "patty_type";
    private static final String BURGER_CHEESE_COLUMN = "cheese";
    private static final String BURGER_SALAD_COLUMN = "salad";
    private static final String BURGER_TOMATO_COLUMN = "tomato";

    private final DatabaseConnection databaseConnection;

    public BurgerDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public List<Burger> getBurgersOfCart(Cart cart) {
        try (Connection connection = databaseConnection.getConnection()) {
            if (connection == null) {
                throw new IllegalStateException();
            }

            String getBurgersOfCartQuery = "SELECT burger.id, burger.patty_type, burger.cheese, burger.salad, burger.tomato"
                    + " FROM burger INNER JOIN cart ON burger.cart_id = cart.id WHERE cart.id = ?;";

            try (PreparedStatement preparedStatement = connection.prepareStatement(getBurgersOfCartQuery)) {
                preparedStatement.setInt(1, cart.getId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    List<Burger> burgerList = new ArrayList<>();

                    while (resultSet.next()) {
                        Burger burger = createBurgerFromResultSet(resultSet);
                        burgerList.add(burger);
                    }

                    return burgerList;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException();
        }
    }

    public boolean persistBurger(Burger burger, Cart cart) {

        try (Connection connection = databaseConnection.getConnection()) {
            if (connection == null) {
                throw new IllegalStateException();
            }
            String insertBurgerStatement = "INSERT INTO burger(patty_type, cheese, salad, tomato, cart_id)" + " VALUES(?, ?, ?, ?, ?);";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertBurgerStatement, PreparedStatement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, burger.getPattyType().name());
                preparedStatement.setBoolean(2, burger.getCheese() != null);
                preparedStatement.setBoolean(3, burger.getSalad() != null);
                preparedStatement.setBoolean(4, burger.getTomato() != null);
                preparedStatement.setInt(5, cart.getId());

                preparedStatement.execute();
                ResultSet resultSet = preparedStatement.getGeneratedKeys();

                if (resultSet.next()) {
                    burger.setId(resultSet.getInt(1));
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteBurgersByID(Cart cart, List<Integer> burgerIdList) {

        try (Connection connection = databaseConnection.getConnection()) {
            if (connection == null) {
                throw new IllegalStateException();
            }

            Array burgerIdsInArray = connection.createArrayOf("integer", burgerIdList.toArray());

            String deleteBurgerStatement = "DELETE FROM burger WHERE cart_id = ? AND id = any (?); ";

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteBurgerStatement)) {
                preparedStatement.setInt(1, cart.getId());
                preparedStatement.setArray(2, burgerIdsInArray);
                preparedStatement.executeUpdate();

                return preparedStatement.getUpdateCount() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Burger createBurgerFromResultSet(ResultSet burgerResultSet) throws SQLException {
        int burgerId = burgerResultSet.getInt(BURGER_ID_COLUMN);
        String pattyTypeString = burgerResultSet.getString(BURGER_PATTY_TYPE_COLUMN);
        PattyType pattyType = PattyType.valueOf(pattyTypeString);

        boolean hasCheese = burgerResultSet.getBoolean(BURGER_CHEESE_COLUMN);
        boolean hasTomato = burgerResultSet.getBoolean(BURGER_TOMATO_COLUMN);
        boolean hasSalad = burgerResultSet.getBoolean(BURGER_SALAD_COLUMN);

        return new Burger(burgerId, pattyType, hasCheese ? new Cheese() : null, hasSalad ? new Salad() : null, hasTomato ? new Tomato() : null);
    }

}
