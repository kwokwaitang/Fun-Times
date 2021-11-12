package com.ford;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

abstract class CashTill {

    protected static final Logger LOGGER = Logger.getGlobal();

    protected List<StockItem> stockItems;

    protected LocalDate dateOfPurchase;

    public CashTill(List<StockItem> stockItems, LocalDate dateOfPurchase) {
        this.stockItems = stockItems;
        this.dateOfPurchase = dateOfPurchase;
    }

    /**
     * Total-up the basket of items and apply any applicable discounts
     *
     * @return The grand total price for the basket
     */
    public BigDecimal priceUp() {
        BigDecimal total = totalUpBasket();

        return total.subtract(getDiscount()).setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Calculate and return any discount
     *
     * @return The discount to be applied to the total price of the basket
     */
    abstract BigDecimal getDiscount();

    /**
     * Total-up the basket (with no discount involved)
     *
     * @return The total price of the goods in the basket
     */
    private BigDecimal totalUpBasket() {
        if (stockItems != null && !stockItems.isEmpty()) {
            return stockItems.stream()
                    .map(stockItem -> stockItem.getCost().multiply(new BigDecimal(stockItem.getRequiredNumber())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return new BigDecimal("0.00");
    }

    protected boolean anyTinsAndLoavesPresentInBasket() {
        boolean tinsPresent = false;
        boolean loavesPresent = false;

        if (stockItems != null && !stockItems.isEmpty()) {
            tinsPresent = stockItems.stream()
                    .anyMatch(stockItem -> stockItem.getUnit().equals(StockItemType.TIN) && stockItem.getProduct().equalsIgnoreCase("soup"));
            loavesPresent = stockItems.stream()
                    .anyMatch(stockItem -> stockItem.getUnit().equals(StockItemType.LOAF) && stockItem.getProduct().equalsIgnoreCase("bread"));

            return (tinsPresent && loavesPresent);
        }

        return false;
    }

    protected boolean anyApplesPresentInBasket() {
        if (stockItems != null && !stockItems.isEmpty()) {
            return stockItems.stream()
                    .anyMatch(stockItem -> stockItem.getUnit().equals(StockItemType.SINGLE) && stockItem.getProduct().equalsIgnoreCase("apple"));
        }

        return false;
    }

    protected boolean moreThan2TinsRequired() {
        if (stockItems != null && !stockItems.isEmpty()) {
            Optional<StockItem> optTinStockItem = stockItems.stream()
                    .filter(stockItem -> stockItem.getUnit().equals(StockItemType.TIN))
                    .findFirst();
            if (optTinStockItem.isPresent()) {
                return (optTinStockItem.get().getRequiredNumber() >= 2);
            }
        }

        return false;
    }

    protected int getNumberOfSoupTinsThatCanBeDiscounted() {
        if (stockItems != null && !stockItems.isEmpty()) {
            Optional<StockItem> optTinStockItem =
                    stockItems.stream().filter(stockItem -> stockItem.getUnit().equals(StockItemType.TIN)).findFirst();
            if (optTinStockItem.isPresent()) {
                return optTinStockItem.get().getRequiredNumber() / 2;
            }
        }

        return 0;
    }

    protected int getNumberOfApplesThatCanBeDiscounted() {
        if (stockItems != null && !stockItems.isEmpty()) {
            Optional<StockItem> optAppleStockItem = stockItems.stream()
                    .filter(stockItem -> stockItem.getUnit().equals(StockItemType.SINGLE) && stockItem.getProduct().equalsIgnoreCase("apple"))
                    .findFirst();
            if (optAppleStockItem.isPresent()) {
                return optAppleStockItem.get().getRequiredNumber();
            }
        }

        return 0;
    }

    /**
     * A check to determine if a discount is available when the date of purchase is between yesterday and in 7 days time
     *
     * @return {@code true} when date of purchase meets the criteria for a discount, otherwise {@code false}
     */
    protected boolean discountAvailableFromYesterdayForSevenDays() {
        LocalDate yesterday = dateOfPurchase.minusDays(1);
        LocalDate forSevenDays = yesterday.plusDays(7);

        return (!dateOfPurchase.isBefore(yesterday) && (dateOfPurchase.isBefore(forSevenDays)));
    }

    /**
     * A check to determine if a discount is available when the date of purchase is from 3 days hence to the end of
     * the current month
     *
     * @return {@code true} when date of purchase meets the criteria for a discount, otherwise {@code false}
     */
    protected boolean discountAvailableFromThreeDaysHenceToEndOfMonth() {
        LocalDate today = LocalDate.now();
        LocalDate inThreeDaysTime = today.plusDays(3);
        LocalDate startDateOfCurrentMonth = dateOfPurchase.withDayOfMonth(1);
        LocalDate endDateOfCurrentMonth = dateOfPurchase.withDayOfMonth(startDateOfCurrentMonth.lengthOfMonth());

        return (dateOfPurchase.isAfter(inThreeDaysTime) && dateOfPurchase.isBefore(endDateOfCurrentMonth));
    }
}
