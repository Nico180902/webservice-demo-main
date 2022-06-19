package xyz.prohinig.webservice.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.modules.junit4.PowerMockRunner;
import xyz.prohinig.webservice.model.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static xyz.prohinig.webservice.database.CartDAO.*;

@ExtendWith(MockitoExtension.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest(CartDAO.class)
class CartDAOTest {
    @Mock
    private DatabaseConnection databaseConnectionMock;
    @Mock
    private Connection connectionMock;
    @Mock
    private Statement statementMock;
    @Mock
    private PreparedStatement preparedStatementMock;
    @Mock
    private ResultSet resultSetMock;
    private CartDAO cartDAO;

    @BeforeEach
    void setUp() {
        cartDAO = new CartDAO(databaseConnectionMock);
    }

    @Test
    void update_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> cartDAO.update(new Cart()));
    }

    @Test
    void update_checksOutCartInDatabaseIfPassedCartIsCheckedOutAndReturnsTrue() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);

        boolean result = cartDAO.update(new Cart(1, true));
        assertTrue(result);

        verify(connectionMock).prepareStatement("UPDATE cart SET active = false WHERE id = ?;");
        verify(preparedStatementMock).setInt(1, 1);
    }

    @Test
    void update_addsCartWithoutIdToDatabaseAndReturnsTrue() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(statementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt(1)).thenReturn(1);

        boolean result = cartDAO.update(new Cart());

        assertTrue(result);

        verify(statementMock).execute("INSERT INTO cart DEFAULT VALUES RETURNING id;", Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    void update_returnsFalseIfResultSetInPersistCartMethodIsEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        boolean result = cartDAO.update(new Cart());

        assertFalse(result);

        verify(statementMock).execute("INSERT INTO cart DEFAULT VALUES RETURNING id;", Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    void update_addsBurgersWithoutIdToCartAndAssignsIdAndReturnsTrue() throws Exception {

        Cart cart = new Cart(1, false);
        Burger firstBurger = new Burger(1, PattyType.MEAT, null, null, null);
        Burger secondBurger = new Burger(PattyType.MEAT, new Cheese(), new Salad(), new Tomato());
        Burger thirdBurger = new Burger(PattyType.VEGGIE, null, null, null);
        cart.addBurger(firstBurger);
        cart.addBurger(secondBurger);
        cart.addBurger(thirdBurger);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(connectionMock.prepareStatement(any(), anyInt())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, true, false);
        when(resultSetMock.getInt(1)).thenReturn(2, 3);

        boolean result = cartDAO.update(cart);

        assertThat(result).isTrue();
        assertThat(secondBurger.getId()).isEqualTo(2);
        assertThat(thirdBurger.getId()).isEqualTo(3);

        verify(connectionMock, times(2))
                .prepareStatement("INSERT INTO burger(patty_type, cheese, salad, tomato, cart_id)"
                        + " VALUES(?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);

        verify(preparedStatementMock).setString(1, "MEAT");
        verify(preparedStatementMock).setBoolean(2, true);
        verify(preparedStatementMock).setBoolean(3, true);
        verify(preparedStatementMock).setBoolean(4, true);
        verify(preparedStatementMock, times (2)).setInt(5, 1);

        verify(preparedStatementMock).setString(1, "VEGGIE");
        verify(preparedStatementMock).setBoolean(2, false);
        verify(preparedStatementMock).setBoolean(3, false);
        verify(preparedStatementMock).setBoolean(4, false);

    }

    @Test
    public void update_doesNotAssignIdsToBurgerAndReturnsFalseIfResultSetInPersistBurgerMethodIsEmpty() throws SQLException {

        Cart cart = new Cart(1, false);
        Burger firstBurger = new Burger(1, PattyType.MEAT, null, null, null);
        Burger secondBurger = new Burger(PattyType.MEAT, new Cheese(), new Salad(), new Tomato());
        Burger thirdBurger = new Burger(PattyType.VEGGIE, null, null, null);
        cart.addBurger(firstBurger);
        cart.addBurger(secondBurger);
        cart.addBurger(thirdBurger);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any(), anyInt())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        boolean result = cartDAO.update(cart);

        assertThat(result).isFalse();
        assertThat(secondBurger.getId()).isNull();
        assertThat(thirdBurger.getId()).isNull();

        verify(connectionMock)
                .prepareStatement("INSERT INTO burger(patty_type, cheese, salad, tomato, cart_id)"
                        + " VALUES(?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);

        verify(preparedStatementMock).setString(1, "MEAT");
        verify(preparedStatementMock).setBoolean(2, true);
        verify(preparedStatementMock).setBoolean(3, true);
        verify(preparedStatementMock).setBoolean(4, true);
        verify(preparedStatementMock).setInt(5, 1);

    }

    @Test
    public void update_executesPreparedStatementWithOneParameterInRetainBurgerMethodIfCartIsEmptyAndReturnsTrue() throws SQLException {
        Cart cart = new Cart(1, false);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);

        boolean result = cartDAO.update(cart);

        assertThat(result).isTrue();


        verify(connectionMock).prepareStatement("DELETE FROM burger WHERE cart_id = ?;");
        verify(preparedStatementMock).setInt(1, cart.getId());
    }

    @Test
    public void update_executesPreparedStatementWithTwoParameterInRetainBurgerMethodIfCartIsFilledAndReturnsTrue() throws SQLException {

        Cart cart = new Cart(1, false);
        Burger burger = new Burger(1, PattyType.MEAT, null, null, null);
        cart.addBurger(burger);

        List<Integer> burgerIdList = new ArrayList<>();
        burgerIdList.add(burger.getId());
        Array burgerIdsInArrayMock = Mockito.mock(Array.class);

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createArrayOf("integer", burgerIdList.toArray())).thenReturn(burgerIdsInArrayMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);

        boolean result = cartDAO.update(cart);

        assertThat(result).isTrue();


        verify(connectionMock).prepareStatement("DELETE FROM burger WHERE cart_id = ? and id != any (?);");
        verify(preparedStatementMock).setInt(1, cart.getId());
        verify(preparedStatementMock).setArray(2, burgerIdsInArrayMock);
    }

    @Test
    void getActiveCart_throwsIllegalStateExceptionIfNullConnectionIsReturned() {

        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> cartDAO.getActiveCart());
    }

    @Test
    void getActiveCart_returnsNewCartWhenResultSetIsEmpty() throws Exception {

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.executeQuery(any())).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        Cart cart = cartDAO.getActiveCart();
        assertThat(cart.getId()).isNull();
        assertThat(cart.getBurgers()).isEmpty();

        verify(statementMock)
                .executeQuery("SELECT burger.* FROM burger INNER JOIN cart ON burger.cart_id = cart.id WHERE cart.active = true;");
    }

    @Test
    void getActiveCart_returnsFilledCartWhenResultSetIsNotEmpty() throws Exception {

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.executeQuery(any())).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, true, false);
        when(resultSetMock.getInt(BURGER_CART_ID_COLUMN)).thenReturn(3);

        when(resultSetMock.getInt(BURGER_ID_COLUMN)).thenReturn(1, 2);
        when(resultSetMock.getString(BURGER_PATTY_TYPE_COLUMN)).thenReturn("VEGGIE", "MEAT");
        when(resultSetMock.getBoolean(BURGER_CHEESE_COLUMN)).thenReturn(false, true);
        when(resultSetMock.getBoolean(BURGER_SALAD_COLUMN)).thenReturn(true, true);
        when(resultSetMock.getBoolean(BURGER_TOMATO_COLUMN)).thenReturn(true, true);

        Cart cart = cartDAO.getActiveCart();

        assertThat(cart.getId()).isEqualTo(3);
        assertThat(cart.getBurgers()).hasSize(2);
        assertThat(cart.getBurgers())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new Burger(1, PattyType.VEGGIE, null, new Salad(), new Tomato()),
                        new Burger(2, PattyType.MEAT, new Cheese(), new Salad(), new Tomato()));

        verify(statementMock)
                .executeQuery("SELECT burger.* FROM burger INNER JOIN cart ON burger.cart_id = cart.id WHERE cart.active = true;");
    }

    @Test
    void getCartByID_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> cartDAO.getCartByID(1));
    }

    @Test
    void getCartById_returnsNullIfNoCartCanBeFoundForIdEntered() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        Cart cart = cartDAO.getCartByID(1);
        assertThat(cart).isNull();


        verify(connectionMock).prepareStatement("SELECT cart.id AS cart_id, cart.active from cart where id = ?");
        verify(preparedStatementMock).setInt(1, 1);
    }

    @Test
    void getCartById_returnsNewCartWhenResultSetIsEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, false);

        when(resultSetMock.getInt("cart_id")).thenReturn(3);
        when(!resultSetMock.getBoolean(CART_ACTIVE_COLUMN)).thenReturn(false);

        Cart cart = cartDAO.getCartByID(3);

        assertThat(cart.getId()).isEqualTo(3);
        assertThat(cart.isCheckedOut()).isTrue();
        assertThat(cart.getBurgers()).isEmpty();

        verify(connectionMock).prepareStatement("SELECT cart.id AS cart_id, cart.active from cart where id = ?");
        verify(connectionMock)
                .prepareStatement("SELECT burger.id, burger.patty_type, burger.cheese, burger.salad, burger.tomato"
                        + " FROM burger INNER JOIN cart ON burger.cart_id = cart.id WHERE cart.id = ?;");

        verify(preparedStatementMock, times(2)).setInt(1, 3);

    }

    @Test
    void getCartById_returnsFilledCartWhenResultSetIsNotEmpty() throws SQLException {

        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, true, true, false);

        when(resultSetMock.getInt("cart_id")).thenReturn(3);
        when(!resultSetMock.getBoolean(CART_ACTIVE_COLUMN)).thenReturn(false);

        when(resultSetMock.getInt(BURGER_ID_COLUMN)).thenReturn(1, 2);
        when(resultSetMock.getString(BURGER_PATTY_TYPE_COLUMN)).thenReturn("VEGGIE", "MEAT");
        when(resultSetMock.getBoolean(BURGER_CHEESE_COLUMN)).thenReturn(false, true);
        when(resultSetMock.getBoolean(BURGER_SALAD_COLUMN)).thenReturn(true, true);
        when(resultSetMock.getBoolean(BURGER_TOMATO_COLUMN)).thenReturn(true, true);

        Cart cart = cartDAO.getCartByID(3);

        assertThat(cart.getId()).isEqualTo(3);
        assertThat(cart.isCheckedOut()).isTrue();
        assertThat(cart.getBurgers()).hasSize(2);

        assertThat(cart.getBurgers())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new Burger(1, PattyType.VEGGIE, null, new Salad(), new Tomato()),
                        new Burger(2, PattyType.MEAT, new Cheese(), new Salad(), new Tomato()));

        verify(connectionMock).prepareStatement("SELECT cart.id AS cart_id, cart.active from cart where id = ?");
        verify(connectionMock).prepareStatement("SELECT burger.id, burger.patty_type, burger.cheese, burger.salad, burger.tomato" + " FROM burger INNER JOIN cart ON burger.cart_id = cart.id WHERE cart.id = ?;");
        verify(preparedStatementMock, times(2)).setInt(1, 3);
    }

    @Test
    void getAllCarts_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> cartDAO.getAllCarts());

    }

    @Test
    void getAllCarts_returnsNewListIfResultSetIsEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.executeQuery(any())).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        List<Cart> cartList = cartDAO.getAllCarts();

        assertThat(cartList).isEmpty();

        verify(statementMock).executeQuery("SELECT burger.*, cart.active FROM burger INNER JOIN cart ON burger.cart_id = cart.id;");

    }

    @Test
    void getAllCarts_returnsFilledListIfResultSetIsNotEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.executeQuery(any())).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, false);

        when(resultSetMock.getInt(BURGER_CART_ID_COLUMN)).thenReturn(1);
        when(!resultSetMock.getBoolean(CART_ACTIVE_COLUMN)).thenReturn(true);

        when(resultSetMock.getInt(BURGER_ID_COLUMN)).thenReturn(1);
        when(resultSetMock.getString(BURGER_PATTY_TYPE_COLUMN)).thenReturn("VEGGIE");
        when(resultSetMock.getBoolean(BURGER_CHEESE_COLUMN)).thenReturn(true);
        when(resultSetMock.getBoolean(BURGER_SALAD_COLUMN)).thenReturn(true);
        when(resultSetMock.getBoolean(BURGER_TOMATO_COLUMN)).thenReturn(true);

        List<Cart> ActualResultList = cartDAO.getAllCarts();

        assertThat(ActualResultList).hasSize(1);

        List<Cart> expectedResultList = new ArrayList<>();
        expectedResultList.add(new Cart(1, false));
        expectedResultList.forEach(cart -> cart.addBurger(new Burger(1, PattyType.VEGGIE, new Cheese(), new Salad(), new Tomato())));

        assertThat(ActualResultList).usingRecursiveFieldByFieldElementComparator().isEqualTo(expectedResultList);
    }

    @Test
    void deleteCart_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> cartDAO.deleteCart(new Cart(1, false)));
    }

    @Test
    void deleteCart_returnsTrueIfGetUpdateCountIsGreaterThanZero() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getUpdateCount()).thenReturn(1);

        boolean result = cartDAO.deleteCart(new Cart(1, false));

        assertTrue(result);

        verify(connectionMock).prepareStatement("DELETE from cart WHERE id = ?;");
        verify(preparedStatementMock).setInt(1, 1);
    }

    @Test
    void deleteCart_returnsFalseIfGetUpdateCountIsLessThanZero() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.getUpdateCount()).thenReturn(-1);

        boolean result = cartDAO.deleteCart(new Cart(1, false));

        assertThat(result).isFalse();

        verify(connectionMock).prepareStatement("DELETE from cart WHERE id = ?;");
        verify(preparedStatementMock).setInt(1, 1);
    }

    @Test
    void createEmptyCart_throwsIllegalStateExceptionIfNullConnectionIsReturned() {
        when(databaseConnectionMock.getConnection()).thenReturn(null);

        assertThatIllegalStateException().isThrownBy(() -> cartDAO.createEmptyCart());
    }

    @Test
    void createEmptyCart_returnsNullIfResultSetIsEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        Cart cart = cartDAO.createEmptyCart();

        assertThat(cart).isNull();

        verify(statementMock).execute("INSERT INTO cart DEFAULT VALUES RETURNING *;", Statement.RETURN_GENERATED_KEYS);


    }

    @Test
    void createEmptyCart_returnsEmptyCartIfResultSetIsNotEmpty() throws SQLException {
        when(databaseConnectionMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(statementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);

        when(resultSetMock.getInt(CART_ID_COLUMN)).thenReturn(1);
        when(!resultSetMock.getBoolean(CART_ACTIVE_COLUMN)).thenReturn(true);

        Cart cart = cartDAO.createEmptyCart();

        assertThat(cart.getId()).isOne();
        assertThat(cart.isCheckedOut()).isFalse();
        assertThat(cart.getBurgers()).isEmpty();

        verify(statementMock).execute("INSERT INTO cart DEFAULT VALUES RETURNING *;", Statement.RETURN_GENERATED_KEYS);
    }
}