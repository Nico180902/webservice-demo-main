package xyz.prohinig.webservice.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.prohinig.webservice.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static xyz.prohinig.webservice.database.CartDAO.*;

@ExtendWith(MockitoExtension.class)
class BurgerDAOTest {
    @Mock
    private DatabaseConnection databaseConnectionMock;
    @Mock
    private Connection connectionMock;
    @Mock
    private PreparedStatement preparedStatementMock;
    @Mock
    private ResultSet resultSetMock;
    private BurgerDAO burgerDAO;

    @BeforeEach
    void setUp() {
        burgerDAO = new BurgerDAO(databaseConnectionMock);
    }

    @Test
    void getBurgersOfCart_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> burgerDAO.getBurgersOfCart(new Cart(1, false)));

    }

    @Test
    void getBurgersOfCart_returnsNewListIfResultSetIsEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        List<Burger> resultList = burgerDAO.getBurgersOfCart(new Cart(1, false));

        assertThat(resultList).isEmpty();
        verify(connectionMock)
                .prepareStatement("SELECT burger.id, burger.patty_type, burger.cheese, burger.salad, burger.tomato"
                        + " FROM burger INNER JOIN cart ON burger.cart_id = cart.id WHERE cart.id = ?;");
        verify(preparedStatementMock).setInt(1, 1);
    }

    @Test
    void getBurgersOfCart_returnsFilledListWhenResultSetIsNotEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, true, false);

        when(resultSetMock.getInt(BURGER_ID_COLUMN)).thenReturn(1, 2);
        when(resultSetMock.getString(BURGER_PATTY_TYPE_COLUMN)).thenReturn("VEGGIE", "MEAT");
        when(resultSetMock.getBoolean(BURGER_CHEESE_COLUMN)).thenReturn(false, true);
        when(resultSetMock.getBoolean(BURGER_SALAD_COLUMN)).thenReturn(true, true);
        when(resultSetMock.getBoolean(BURGER_TOMATO_COLUMN)).thenReturn(true, true);

        List<Burger> resultList = burgerDAO.getBurgersOfCart(new Cart(1, false));

        assertThat(resultList).hasSize(2);
        assertThat(resultList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new Burger(1, PattyType.VEGGIE, null, new Salad(), new Tomato()),
                        new Burger(2, PattyType.MEAT, new Cheese(), new Salad(), new Tomato()));

        verify(connectionMock)
                .prepareStatement("SELECT burger.id, burger.patty_type, burger.cheese, burger.salad, burger.tomato"
                        + " FROM burger INNER JOIN cart ON burger.cart_id = cart.id WHERE cart.id = ?;");
        verify(preparedStatementMock).setInt(1, 1);
    }

    @Test
    void persistBurger_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        Burger burger = new Burger(1, PattyType.MEAT, null, null, null);
        Cart cart = new Cart(1, false);

        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> burgerDAO.persistBurger(burger, cart));
    }

    @Test
    void persistBurger_addsBurgerToDatabaseAndAssignsIdToBurgerAndReturnsTrueIfResultSetIsNotEmpty() throws SQLException {
        Burger burger = new Burger(PattyType.MEAT, new Cheese(), null, new Tomato());
        Cart cart = new Cart(1, false);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any(), anyInt())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt(1)).thenReturn(1);

        boolean result = burgerDAO.persistBurger(burger, cart);

        assertThat(result).isTrue();
        assertThat(burger.getId()).isOne();

        verify(connectionMock).prepareStatement("INSERT INTO burger(patty_type, cheese, salad, tomato, cart_id)"
                + " VALUES(?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
        verify(preparedStatementMock).setString(1, burger.getPattyType().name());
        verify(preparedStatementMock).setBoolean(2, burger.getCheese() != null);
        verify(preparedStatementMock).setBoolean(3, burger.getSalad() != null);
        verify(preparedStatementMock).setBoolean(4, burger.getTomato() != null);
        verify(preparedStatementMock).setInt(5, cart.getId());

    }

    @Test
    void persistBurger_doesNotAssignIdToBurgerAndReturnsFalseIfResultSetIsEmpty() throws SQLException {
        Burger burger = new Burger(PattyType.MEAT, new Cheese(), null, new Tomato());
        Cart cart = new Cart(1, false);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any(), anyInt())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        boolean result = burgerDAO.persistBurger(burger, cart);

        assertThat(result).isFalse();
        assertThat(burger.getId()).isNull();

        verify(connectionMock).prepareStatement("INSERT INTO burger(patty_type, cheese, salad, tomato, cart_id)"
                + " VALUES(?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
        verify(preparedStatementMock).setString(1, burger.getPattyType().name());
        verify(preparedStatementMock).setBoolean(2, burger.getCheese() != null);
        verify(preparedStatementMock).setBoolean(3, burger.getSalad() != null);
        verify(preparedStatementMock).setBoolean(4, burger.getTomato() != null);
        verify(preparedStatementMock).setInt(5, cart.getId());
    }

    @Test
    void deleteBurgersByID_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        Cart cart = new Cart(1, false);
        List<Integer> burgerIdList = new ArrayList<>();

        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> burgerDAO.deleteBurgersById(cart, burgerIdList));
    }

    @Test
    void deleteBurgersById_returnsFalseIfGetUpdateCountIsLessThanZero() throws SQLException {
        Cart cart = new Cart(1, false);
        List<Integer> burgerIdList = new ArrayList<>();
        Array burgerIdsInArrayMock = Mockito.mock(Array.class);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createArrayOf("integer", burgerIdList.toArray())).thenReturn(burgerIdsInArrayMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getUpdateCount()).thenReturn(-1);

        boolean result = burgerDAO.deleteBurgersById(cart, burgerIdList);

        assertThat(result).isFalse();

        verify(connectionMock).prepareStatement("DELETE FROM burger WHERE cart_id = ? AND id = any (?);");
        verify(preparedStatementMock).setInt(1, cart.getId());
        verify(preparedStatementMock).setArray(2, burgerIdsInArrayMock);
    }

    @Test
    void deleteBurgersById_returnsTrueIfGetUpdateCountIsGreaterThanZero() throws SQLException {
        Cart cart = new Cart(1, false);
        List<Integer> burgerIdList = new ArrayList<>();
        Array burgerIdsInArrayMock = Mockito.mock(Array.class);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createArrayOf("integer", burgerIdList.toArray())).thenReturn(burgerIdsInArrayMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getUpdateCount()).thenReturn(1);

        boolean result = burgerDAO.deleteBurgersById(cart, burgerIdList);

        assertThat(result).isTrue();

        verify(connectionMock).prepareStatement("DELETE FROM burger WHERE cart_id = ? AND id = any (?);");
        verify(preparedStatementMock).setInt(1, cart.getId());
        verify(preparedStatementMock).setArray(2, burgerIdsInArrayMock);
    }
}