package com.ford.discount;

import com.ford.stockitem.StockItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Discounts starting from 3 days hence to the end of the month
 */
public class FromThreeDaysHenceToEndOfMonth extends Discount {

    public FromThreeDaysHenceToEndOfMonth(List<StockItem> stockItems, LocalDate dateOfPurchase) {
        super(stockItems, dateOfPurchase);
    }

    @Override
    BigDecimal calculateDiscount() {
        BigDecimal discount = new BigDecimal("0.00");

        if (isDiscountAvailableByDateOfPurchase()) {
            if (anySingleItemsPresentInBasket(SINGLE_ITEM_APPLE)) {
                BigDecimal subTotal =
                        new BigDecimal(Integer.toString(getCountOfSingleItem(SINGLE_ITEM_APPLE))).multiply(new BigDecimal(
                                "0.10")); // TODO fetch price of apples
                discount = discount.add(subTotal.multiply(BigDecimal.valueOf(APPLES_DISCOUNT)));
            }

            // Any other discounts...
        }

        return discount;
    }

    private boolean isDiscountAvailableByDateOfPurchase() {
        LocalDate today = LocalDate.now();
        LocalDate inThreeDaysTime = today.plusDays(3);
        LocalDate startDateOfCurrentMonth = dateOfPurchase.withDayOfMonth(1);
        LocalDate endDateOfCurrentMonth = dateOfPurchase.withDayOfMonth(startDateOfCurrentMonth.lengthOfMonth());

        return (dateOfPurchase.isAfter(inThreeDaysTime) && dateOfPurchase.isBefore(endDateOfCurrentMonth));
    }
}
