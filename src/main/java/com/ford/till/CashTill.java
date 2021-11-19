package com.ford.till;

import com.ford.discount.Discount;
import com.ford.discount.FromThreeDaysHenceToEndOfMonth;
import com.ford.discount.FromYesterdayForSevenDays;
import com.ford.stockitem.StockItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class CashTill implements Till {

    private List<StockItem> stockItems;

    private LocalDate dateOfPurchase;

    public CashTill(List<StockItem> stockItems, LocalDate dateOfPurchase) {
        this.stockItems = stockItems;
        this.dateOfPurchase = dateOfPurchase;
    }

    @Override
    public BigDecimal priceUp() {
        BigDecimal total = totalUp();

        // Ensure there is a grand total before applying any possible discounts
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate any discounts...
            Discount fromYesterdayForSevenDays = new FromYesterdayForSevenDays(stockItems, dateOfPurchase);
            Discount fromThreeDaysHenceToEndOfMonth = new FromThreeDaysHenceToEndOfMonth(stockItems, dateOfPurchase);

            BigDecimal discount = fromYesterdayForSevenDays.getDiscount();
            discount = discount.add(fromThreeDaysHenceToEndOfMonth.getDiscount());

            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                return total.subtract(discount).setScale(2, RoundingMode.HALF_EVEN);
            }
        }

        return total.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal totalUp() {
        if (stockItems != null && !stockItems.isEmpty()) {
            return stockItems.stream()
                    .map(stockItem -> stockItem.getCost().multiply(new BigDecimal(stockItem.getRequiredNumber())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return new BigDecimal("0.00");
    }
}
