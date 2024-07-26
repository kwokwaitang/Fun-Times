package com.ford;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shopping {
    public static final String DISCOUNT_OFFER_REGEX = "^(.*) (?:has|have) a (\\d+)% discount$";
    public static final String DISCOUNT_HALF_PRICE_OFFER_REGEX = "^buy (\\d+) (.*) and get (.*) half price$";
    public static final String NUMBER_OF_STOCK_ITEMS_REGEX = "^(\\d+) (?:(?:tin|tins) of soup|(?:loaf|loaves) of bread|(?:bottle|bottles) of milk|(?:apple|apples))$";
    public static final String VALID_TO_SPECIFIC_DAYS_REGEX = "^for \\d+ day[s]$";
    public static final String VALID_FROM_SPECIFIC_DAYS_REGEX = "^from \\d+ day[s] hence$";
    public static final int DEFAULT_DAYS_IN_ADVANCE = 3;
    public static final String SOUP = "soup";
    public static final String BREAD = "bread";
    public static final String MILK = "milk";
    public static final String APPLE = "apple";
    public static final String APPLES = "apples";

    private Map<String, StockItem> shoppingBasket;

    private StockItem soupStock;
    private StockItem breadStock;
    private StockItem milkStock;
    private StockItem applesStock;

    private List<Discount> discounts;

    private LocalDate toBeBoughtOn;

    private BigDecimal totalWithNoDiscounts;
    private BigDecimal totalDiscount;

    public Shopping() {
        shoppingBasket = new HashMap<>();
        discounts = Collections.emptyList();
        totalWithNoDiscounts = new BigDecimal("0.00");
        totalDiscount = new BigDecimal("0.00");
    }

    @SuppressWarnings("java:S112")
    public void setupStockItems(final List<List<String>> rows) {
        rows.stream().skip(1) /* The 1st row as it is just column headings */.forEach((List<String> columns) -> {
            final var product = columns.get(0);
            final var cost = new BigDecimal(columns.get(2));
            switch (product) {
                case SOUP -> soupStock = StockItem.create(product, StockItemType.TIN, cost, 0);
                case BREAD -> breadStock = StockItem.create(product, StockItemType.LOAF, cost, 0);
                case MILK -> milkStock = StockItem.create(product, StockItemType.BOTTLE, cost, 0);
                case APPLE -> applesStock = StockItem.create(product, StockItemType.SINGLE, cost, 0);
                default -> throw new RuntimeException("Unsupported stock item type: " + columns.get(0));
            }
        });
    }

    public void setupDiscounts(final List<List<String>> rows) {
        if (!rows.isEmpty()) {
            discounts = new ArrayList<>(rows.size());
            rows.stream().skip(1) /* The column headings */.forEach((List<String> columns) -> {
                final var theOffer = columns.get(0);
                final var validFrom = columns.get(1);
                final var validTo = columns.get(2);

                discounts.add(new Discount(theOffer, validFrom, validTo));
            });
        }
    }

    public void emptyShoppingBasket() {
        shoppingBasket = new HashMap<>();
    }

    public void updateShoppingBasketWithRequiredNumber(String requestedItem) {
        if (requestedItem.matches(NUMBER_OF_STOCK_ITEMS_REGEX)) {
            var matcher = getPatternMatcherByRegex(NUMBER_OF_STOCK_ITEMS_REGEX, requestedItem);
            if (matcher.find()) {
                var requiredNumberOfItems = Integer.parseInt(matcher.group(1));
                if (requestedItem.contains(SOUP)) {
                    soupStock.requiredNumber = requiredNumberOfItems;
                    shoppingBasket.put(SOUP, soupStock);
                } else if (requestedItem.contains(BREAD)) {
                    breadStock.requiredNumber = requiredNumberOfItems;
                    shoppingBasket.put(BREAD, breadStock);
                } else if (requestedItem.contains(MILK)) {
                    milkStock.requiredNumber = requiredNumberOfItems;
                    shoppingBasket.put(MILK, milkStock);
                } else if (requestedItem.contains(APPLE)) {
                    applesStock.requiredNumber = requiredNumberOfItems;
                    shoppingBasket.put(APPLES, applesStock);
                }
            }
        }
    }

    private Matcher getPatternMatcherByRegex(final String regex, final String input) {
        var pattern = Pattern.compile(regex);
        return pattern.matcher(input);
    }

    public void determineWhenToBeBought(String dateOfPurchase) {
        if (dateOfPurchase.equals("today")) {
            toBeBoughtOn = LocalDate.now();
        } else {
            var days = getDaysInAdvance(dateOfPurchase).orElse(DEFAULT_DAYS_IN_ADVANCE);
            toBeBoughtOn = LocalDate.now().plusDays(days);
        }
    }

    private Optional<Integer> getDaysInAdvance(String dateOfPurchase) {
        Optional<Integer> daysInAdvance = Optional.empty();

        var matcher = getPatternMatcherByRegex("^in (\\d+) days time$", dateOfPurchase);
        if (matcher.find()) {
            daysInAdvance = Optional.of(Integer.valueOf(matcher.group(1)));
        }

        return daysInAdvance;
    }

    public void totalUpShoppingBasket() {
        List<StockItem> stockItems = new ArrayList<>(shoppingBasket.values());
        stockItems.stream()
                .filter(stockItem -> stockItem.requiredNumber != 0)
                .forEach(stockItem -> {
                    BigDecimal subTotal = stockItem.cost.multiply(BigDecimal.valueOf(stockItem.requiredNumber));
                    totalWithNoDiscounts = totalWithNoDiscounts.add(subTotal);
                });
    }

    public void applyDiscount() {
        // Need to know what discounts are applicable and what is in the shopping basket
        discounts.forEach(currentDiscount -> {
            if (isBuySomeProductsAndGetSomethingHalfPrice(currentDiscount)) {
                applyDiscountToBuySomeProductsAndGetSomethingHalfPrice(currentDiscount);
            }

            if (isProductWithSomeDiscount(currentDiscount)) {
                applyProductWithSomeDiscount(currentDiscount);
            }
        });
    }

    private void applyDiscountToBuySomeProductsAndGetSomethingHalfPrice(Discount currentDiscount) {
        var optionalDiscountDetails = getProductsAndSomethingForHalfPriceValues(currentDiscount.offer());
        if (optionalDiscountDetails.isPresent()) {
            var validToSpecificDays = getValidSpecificDays(currentDiscount.validTo(), VALID_TO_SPECIFIC_DAYS_REGEX);
            if (discountAvailableFromYesterdayForSpecifiedDays(validToSpecificDays)) {
                Triple<Integer, String, String> discountDetails = optionalDiscountDetails.get();
                StockItem stockItem = getStockItemFromShoppingBasketByName(discountDetails.getMiddle());
                if (isStockItemRequired(stockItem)) {
                    var halfPriceItem = getHalfPriceItem(discountDetails.getRight());
                    totalDiscount = totalDiscount.add(halfPriceItem).setScale(2, RoundingMode.HALF_EVEN);
                }
            }
        }
    }

    private void applyProductWithSomeDiscount(Discount currentDiscount) {
        var optionalDiscountDetails = getProductAndPercentageValues(currentDiscount.offer());
        if (optionalDiscountDetails.isPresent()) {
            var validFromSpecificDays = getValidSpecificDays(currentDiscount.validFrom(), VALID_FROM_SPECIFIC_DAYS_REGEX);
            if (discountAvailableFromSpecifiedDaysHenceToEndOfMonth(validFromSpecificDays)) {
                var discountDetails = optionalDiscountDetails.get();

                // Does the basket have some products, if so, apply a discount
                StockItem stockItem = shoppingBasket.get(discountDetails.getLeft());
                if (isStockItemRequired(stockItem)) {
                    // Total-up number of apples and apply a 10% discount
                    final String theDiscountOffer = "0." + discountDetails.getRight();
                    BigDecimal subTotal = new BigDecimal(Integer.toString(stockItem.requiredNumber))
                            .multiply(new BigDecimal(theDiscountOffer));

                    var discount = new BigDecimal("0.00");
                    discount = discount.add(subTotal.multiply(BigDecimal.valueOf(Double.parseDouble(theDiscountOffer))));

                    totalDiscount = totalDiscount.add(discount).setScale(2, RoundingMode.HALF_EVEN);
                }
            }
        }
    }

    private StockItem getStockItemFromShoppingBasketByName(final String nameOfStockItem) {
        return switch(nameOfStockItem) {
            case "tin of soup", "tins of soup" -> shoppingBasket.get(SOUP);
            case "loaf of bread", "loaves of bread" -> shoppingBasket.get(BREAD);
            case "bottle of milk", "bottles of milk" -> shoppingBasket.get(MILK);
            case APPLE, APPLES -> shoppingBasket.get(APPLES);
            default -> null;
        };
    }

    private BigDecimal getHalfPriceItem(final String nameOfHalfPricedStockItem) {
        return switch(nameOfHalfPricedStockItem) {
            case "a tin of soup" -> shoppingBasket.get(SOUP).cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
            case "a loaf of bread" -> shoppingBasket.get(BREAD).cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
            case "a bottle of milk" -> shoppingBasket.get(MILK).cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
            case "an apple" -> shoppingBasket.get(APPLES).cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
            default -> new BigDecimal("0.00");
        };
    }

    /**
     * Discount rule, for example:
     * <p/>
     * Buy...
     * <ul>
     *     <li>1 tin of soup / x tins of soup</li>
     *     <li>1 loaf of bread / x loaves of bread</li>
     *     <li>1 bottle of milk / x bottles of milk</li>
     *     <li>1 apple / x apples</li>
     * </ul>
     * <p/>
     * and get...
     * <ul>
     *     <li>tin of soup</li>
     *     <li>loaf of bread</li>
     *     <li>bottle of milk</li>
     *     <li>[a|n] apple, banana etc.</li>
     * </ul>
     * half price
     * <p/>
     *
     * @param discount
     * @return
     */
    private boolean isBuySomeProductsAndGetSomethingHalfPrice(Discount discount) {
        return discount.offer().matches(DISCOUNT_HALF_PRICE_OFFER_REGEX);
    }

    private Optional<Triple<Integer /* number of stock items */, String /* stock item */, String /* half-priced stock item */>> getProductsAndSomethingForHalfPriceValues(String offer) {
        Optional<Triple<Integer, String, String>> details = Optional.empty();

        var matcher = getPatternMatcherByRegex(DISCOUNT_HALF_PRICE_OFFER_REGEX, offer);
        if (matcher.find()) {
            details = Optional.of(
                    ImmutableTriple.of(
                            Integer.valueOf(matcher.group(1)),
                            matcher.group(2),
                            matcher.group(DEFAULT_DAYS_IN_ADVANCE)));
        }

        return details;
    }

    private static int getValidSpecificDays(String validSpecificDays, final String regex) {
        int validToSpecificDays = 0;

        if (StringUtils.isNoneBlank(validSpecificDays) && validSpecificDays.matches(regex)) {
            validToSpecificDays = Integer.parseInt(validSpecificDays.split(" ")[1]);
        }

        return validToSpecificDays;
    }

    private boolean discountAvailableFromYesterdayForSpecifiedDays(final int specifiedDays) {
        LocalDate yesterday = toBeBoughtOn.minusDays(1);
        LocalDate forSpecifiedDays = toBeBoughtOn.plusDays(specifiedDays);

        return (!toBeBoughtOn.isBefore(yesterday) && (toBeBoughtOn.isBefore(forSpecifiedDays)));
    }

    private static boolean isStockItemRequired(StockItem stockItem) {
        return stockItem != null && stockItem.requiredNumber > 0;
    }

    /**
     * Discount rule, for example:
     * <ul>
     *     <li>Tins of soup have a 10% discount</li>
     *     <li>Loaves of bread have a 10% discount</li>
     *     <li>Bottles of milk have a 10% discount</li>
     *     <li>Apples have a 10% discount</li>
     * </ul>
     *
     * @param discount
     * @return
     */
    private boolean isProductWithSomeDiscount(Discount discount) {
        return discount.offer().matches(DISCOUNT_OFFER_REGEX);
    }

    private Optional<Pair<String /* product */, Integer /* percentage discount */>> getProductAndPercentageValues(String offer) {
        Optional<Pair<String, Integer>> extractedDetails = Optional.empty();

        var matcher = getPatternMatcherByRegex(DISCOUNT_OFFER_REGEX, offer);
        if (matcher.find()) {
            extractedDetails = Optional.of(
                    new ImmutablePair<>(
                            matcher.group(1),
                            Integer.valueOf(matcher.group(2))));
        }

        return extractedDetails;
    }

    private boolean discountAvailableFromSpecifiedDaysHenceToEndOfMonth(final int days) {
        LocalDate today = LocalDate.now();
        LocalDate specifiedDays = today.plusDays(days);
        LocalDate startDateOfCurrentMonth = toBeBoughtOn.withDayOfMonth(1);
        LocalDate endDateOfCurrentMonth = toBeBoughtOn.withDayOfMonth(startDateOfCurrentMonth.lengthOfMonth());

        boolean isAfterSpecifiedDays = toBeBoughtOn.isAfter(specifiedDays);
        boolean isBeforeOrOnTheEndOfTheMonth =
                toBeBoughtOn.isBefore(endDateOfCurrentMonth) || toBeBoughtOn.isEqual(endDateOfCurrentMonth);

        return isAfterSpecifiedDays && isBeforeOrOnTheEndOfTheMonth;
    }

    public BigDecimal getTotalWithNoDiscounts() {
        return totalWithNoDiscounts;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }
}
