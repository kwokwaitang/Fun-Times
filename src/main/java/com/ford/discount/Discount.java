package com.ford.discount;

import com.ford.stockitem.StockItem;
import com.ford.stockitem.StockItemType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public abstract class Discount {

    protected static final String SINGLE_ITEM_APPLE = "apple";
    protected static final Double APPLES_DISCOUNT = 0.10;

    protected final List<StockItem> stockItems;

    protected final LocalDate dateOfPurchase;

    protected Discount(List<StockItem> stockItems, LocalDate dateOfPurchase) {
        this.stockItems = stockItems;
        this.dateOfPurchase = dateOfPurchase;
    }

    public BigDecimal getDiscount() {
        BigDecimal discount = new BigDecimal("0.00");

        if (stockItems != null && !stockItems.isEmpty()) {
            discount = discount.add(calculateDiscount());
        }

        return discount;
    }

    abstract BigDecimal calculateDiscount();

    protected boolean anyStockItemsPresentInBasket(List<StockItemType> stockItemTypes) {
        return stockItems.stream()
                .anyMatch(stockItem -> stockItemTypes.contains(stockItem.getUnit()));
    }

    protected boolean isTinsInExcessOver(final int criteria) {
        Optional<StockItem> optTinStockItem = stockItems.stream()
                .filter(stockItem -> stockItem.getUnit().equals(StockItemType.TIN))
                .findFirst();
        if (optTinStockItem.isPresent()) {
            return (optTinStockItem.get().getRequiredNumber() >= criteria);
        }

        return false;
    }

    protected int getNumberOfSoupTinsThatCanBeDiscounted() {
        Optional<StockItem> optTinStockItem =
                stockItems.stream().filter(stockItem -> stockItem.getUnit().equals(StockItemType.TIN)).findFirst();
        if (optTinStockItem.isPresent()) {
            return optTinStockItem.get().getRequiredNumber() / 2;
        }

        return 0;
    }

    protected boolean anyApplesPresentInBasket() {
        return stockItems.stream()
                .anyMatch(stockItem -> stockItem.getUnit().equals(StockItemType.SINGLE) && stockItem.getProduct().equalsIgnoreCase("apple"));
    }

    protected boolean anySingleItemsPresentInBasket(final String singleItem) {
        return stockItems.stream()
                .anyMatch(stockItem -> stockItem.getUnit().equals(StockItemType.SINGLE) && stockItem.getProduct().equalsIgnoreCase(singleItem));
    }

    protected int getNumberOfApplesThatCanBeDiscounted() {
        Optional<StockItem> optAppleStockItem = stockItems.stream()
                .filter(stockItem -> stockItem.getUnit().equals(StockItemType.SINGLE) && stockItem.getProduct().equalsIgnoreCase("apple"))
                .findFirst();
        if (optAppleStockItem.isPresent()) {
            return optAppleStockItem.get().getRequiredNumber();
        }

        return 0;
    }

    protected int getCountOfSingleItem(final String singleItem) {
        Optional<StockItem> optAppleStockItem = stockItems.stream()
                .filter(stockItem -> stockItem.getUnit().equals(StockItemType.SINGLE) && stockItem.getProduct().equalsIgnoreCase(singleItem))
                .findFirst();
        if (optAppleStockItem.isPresent()) {
            return optAppleStockItem.get().getRequiredNumber();
        }

        return 0;
    }
}
