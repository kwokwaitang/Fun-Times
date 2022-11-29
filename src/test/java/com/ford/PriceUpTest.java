package com.ford;

import com.ford.basket.Basket;
import com.ford.basket.HenryGroceryBasket;
import com.ford.stockitem.StockItem;
import com.ford.stockitem.StockItemType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceUpTest {

    private static final LocalDate PURCHASE_TODAY = LocalDate.now();
    private static final LocalDate PURCHASE_IN_FIVE_DAYS_TIME = LocalDate.now().plusDays(5);

    @Test
    @DisplayName("When the basket of goods is unavailable")
    void constructorWithMissingBasket() {
        Exception exception = Assertions.assertThrows(NullPointerException.class, () -> {
            new HenryGroceryBasket(null, PURCHASE_TODAY);
        });

        String expectedMessage = "Basket unavailable";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("When the date of purchase is missing")
    void constructorWithMissingDateOfPurchase() {
        List<StockItem> emptyBasket = new ArrayList<>();

        Exception exception = Assertions.assertThrows(NullPointerException.class, () -> {
            new HenryGroceryBasket(emptyBasket, null);
        });

        String expectedMessage = "Missing a date of purchase";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Price up basket with 3 tins and 2 loafs of bread, purchase today")
    void shouldPriceUp_BasketOf3Tins2LoafsOfBread_BoughtToday() {
        // Given...
        StockItem soupTins = new StockItem("soup", StockItemType.TIN, new BigDecimal("0.65"), 3);
        StockItem loavesOfBread = new StockItem("bread", StockItemType.LOAF, new BigDecimal("0.80"), 2);
        List<StockItem> basket = new ArrayList<>(Arrays.asList(soupTins, loavesOfBread));

        Basket henryGroceryBasket = new HenryGroceryBasket(basket);

        // When...
        BigDecimal grandTotal = henryGroceryBasket.priceUp();

        // Then...
        BigDecimal expected = new BigDecimal("3.15");
        assertTrue(expected.compareTo(grandTotal) == 0);
    }

    @Test
    @DisplayName("Price up basket with 6 apples and 1 bottle of milk, purchase today")
    void shouldPriceUp_BasketOf6Apples1Milk_BoughtToday() {
        // Given...
        StockItem apples = new StockItem("apple", StockItemType.SINGLE, new BigDecimal("0.10"), 6);
        StockItem bottleOfMilk = new StockItem("milk", StockItemType.BOTTLE, new BigDecimal("1.30"), 1);
        List<StockItem> basket = new ArrayList<>(Arrays.asList(apples, bottleOfMilk));

        Basket henryGroceryBasket = new HenryGroceryBasket(basket, PURCHASE_TODAY);

        // When...
        BigDecimal grandTotal = henryGroceryBasket.priceUp();

        // Then...
        BigDecimal expected = new BigDecimal("1.90");

        assertEquals(0, expected.compareTo(grandTotal));
    }

    @Test
    @DisplayName("Price up basket with 6 apples and 1 bottle of milk, to be purchased in 5 days time")
    void shouldPriceUp_BasketOf6Apples1Milk_BoughtIn5DaysTime() {
        // Given...
        StockItem apples = new StockItem("apple", StockItemType.SINGLE, new BigDecimal("0.10"), 6);
        StockItem bottleOfMilk = new StockItem("milk", StockItemType.BOTTLE, new BigDecimal("1.30"), 1);
        List<StockItem> basket = new ArrayList<>(Arrays.asList(apples, bottleOfMilk));

        Basket henryGroceryBasket = new HenryGroceryBasket(basket, PURCHASE_IN_FIVE_DAYS_TIME);

        // When...
        BigDecimal grandTotal = henryGroceryBasket.priceUp();

        // Then...
        BigDecimal expected = new BigDecimal("1.84");

        assertEquals(0, expected.compareTo(grandTotal));
    }

    @Test
    @DisplayName("Price up basket with 3 apples, 2 tins of soup and 1 loaf of bread, to be purchased in 5 days time")
    void shouldPriceUp_BasketOf3Apples2Tins1LoafOfBread_BoughtIn5DaysTime() {
        // Given...
        StockItem apples = new StockItem("apple", StockItemType.SINGLE, new BigDecimal("0.10"), 3);
        StockItem soupTins = new StockItem("soup", StockItemType.TIN, new BigDecimal("0.65"), 2);
        StockItem loafOfBread = new StockItem("bread", StockItemType.LOAF, new BigDecimal("0.80"), 1);
        List<StockItem> basket = new ArrayList<>(Arrays.asList(apples, soupTins, loafOfBread));

        Basket henryGroceryBasket = new HenryGroceryBasket(basket, PURCHASE_IN_FIVE_DAYS_TIME);

        // When...
        BigDecimal grandTotal = henryGroceryBasket.priceUp();

        // Then...
        BigDecimal expected = new BigDecimal("1.97");

        assertEquals(0, expected.compareTo(grandTotal));
    }

    @Test
    void theCompareToOfStockItem() {
        StockItem apples = new StockItem("apple", StockItemType.SINGLE, new BigDecimal("0.10"), 3);
        StockItem soupTins = new StockItem("soup", StockItemType.TIN, new BigDecimal("0.65"), 2);
        StockItem loafOfBread = new StockItem("bread", StockItemType.LOAF, new BigDecimal("0.80"), 1);
        StockItem bottleOfMilk = new StockItem("milk", StockItemType.BOTTLE, new BigDecimal("1.30"), 1);
        Set<StockItem> basket = new TreeSet<>(Arrays.asList(loafOfBread, bottleOfMilk, soupTins, apples));

//        System.out.println(basket);

        assertThat(basket.isEmpty(), is(Boolean.FALSE));

        List<StockItem> stockItems = basket.stream().collect(Collectors.toList());
        assertThat(stockItems.get(0).getProduct(), is("apple"));
        assertThat(stockItems.get(1).getProduct(), is("soup"));
        assertThat(stockItems.get(2).getProduct(), is("bread"));
        assertThat(stockItems.get(3).getProduct(), is("milk"));

        // Alternatively...
        if (basket.toArray() instanceof StockItem[]) {
            StockItem[] stockItemsAsArray = (StockItem[]) basket.toArray();
            assertThat(stockItemsAsArray[0].getProduct(), is("apple"));
            assertThat(stockItemsAsArray[1].getProduct(), is("soup"));
            assertThat(stockItemsAsArray[2].getProduct(), is("bread"));
            assertThat(stockItemsAsArray[3].getProduct(), is("milk"));
        }
    }
}
