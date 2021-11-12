package com.ford;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HenryGroceryBasket implements Basket {

    private List<StockItem> basket;

    private LocalDate dateOfPurchase;

    public HenryGroceryBasket(List<StockItem> basket) {
        this(basket, LocalDate.now());
    }

    public HenryGroceryBasket(List<StockItem> basket, LocalDate dateOfPurchase) {
        this.basket = Objects.requireNonNull(basket, () -> "Basket unavailable");
        this.dateOfPurchase = Objects.requireNonNull(dateOfPurchase, () -> "Missing a date of purchase");
    }

    public List<StockItem> getBasket() {
        return Collections.unmodifiableList(basket);
    }

    public void setBasket(List<StockItem> basket) {
        this.basket = basket;
    }

    /**
     * Need to pass in the purchase date to determine what discount is applicable
     *
     * @return The totalled up price of the goods in the basket
     */
    @Override
    public BigDecimal priceUp() {
        CashTill discount = new CurrentDiscount(basket, dateOfPurchase);
        return discount.priceUp();
    }
}
