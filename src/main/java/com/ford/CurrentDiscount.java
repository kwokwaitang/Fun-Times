package com.ford;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CurrentDiscount extends CashTill {

    private static final Double APPLES_DISCOUNT = 0.10;

    public CurrentDiscount(List<StockItem> stockItems, LocalDate dateOfPurchase) {
        super(stockItems, dateOfPurchase);
    }

    @Override
    BigDecimal getDiscount() {
        BigDecimal discount = new BigDecimal("0.00");

        if (discountAvailableFromYesterdayForSevenDays()) {
            if (anyTinsAndLoavesPresentInBasket()) {
                if (moreThan2TinsRequired()) {
                    BigDecimal subTotal =
                            new BigDecimal("0.40").multiply(new BigDecimal(Integer.toString(getNumberOfSoupTinsThatCanBeDiscounted())));
                    discount = discount.add(subTotal);
                }
            }
        }

        if (discountAvailableFromThreeDaysHenceToEndOfMonth()) {
            if (anyApplesPresentInBasket()) {
                // Total-up number of apples and apply a 10% discount
                BigDecimal subTotal =
                        new BigDecimal(Integer.toString(getNumberOfApplesThatCanBeDiscounted()))
                                .multiply(new BigDecimal("0.10")); // TODO fetch price of apples
                discount = discount.add(subTotal.multiply(BigDecimal.valueOf(APPLES_DISCOUNT)));
            }
        }

        return discount;
    }
}
