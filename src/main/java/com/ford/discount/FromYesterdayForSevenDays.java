package com.ford.discount;

import com.ford.stockitem.StockItem;
import com.ford.stockitem.StockItemType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Discounts starting from yesterday and lasting for 7 days
 */
public class FromYesterdayForSevenDays extends Discount {

    public FromYesterdayForSevenDays(List<StockItem> stockItems, LocalDate dateOfPurchase) {
        super(stockItems, dateOfPurchase);
    }

    @Override
    BigDecimal calculateDiscount() {
        BigDecimal discount = new BigDecimal("0.00");

        if (isDiscountAvailableByDateOfPurchase()) {
            if (anyStockItemsPresentInBasket(Arrays.asList(StockItemType.TIN, StockItemType.LOAF))) {
                if (isTinsInExcessOver(2)) {
                    BigDecimal subTotal =
                            new BigDecimal("0.40").multiply(new BigDecimal(Integer.toString(getNumberOfSoupTinsThatCanBeDiscounted())));
                    discount = discount.add(subTotal);
                }
            }

            // Any other discounts...
        }

        return discount;
    }

    private boolean isDiscountAvailableByDateOfPurchase() {
        LocalDate yesterday = dateOfPurchase.minusDays(1);
        LocalDate forSevenDays = yesterday.plusDays(7);

        return (!dateOfPurchase.isBefore(yesterday) && (dateOfPurchase.isBefore(forSevenDays)));
    }
}
