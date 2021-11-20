package com.ford.basket;

import com.ford.stockitem.StockItem;
import com.ford.till.CashTill;
import com.ford.till.Till;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HenryGroceryBasket implements Basket {

    private List<StockItem> basket;

    private final LocalDate dateOfPurchase;

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

    @Override
    public BigDecimal priceUp() {
        final Till till = new CashTill(basket, dateOfPurchase);

        return till.priceUp();
    }
}
