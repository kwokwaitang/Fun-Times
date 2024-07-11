package com.ford;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class ShoppingSteps implements En {

    public static final String DISCOUNT_OFFER = "^(.*) (?:has|have) a (\\d+)% discount$";
    public static final String DISCOUNT_HALF_PRICE_OFFER = "^buy (\\d+) (.*) and get (.*) half price$";
    public static final String NUMBER_OF_STOCK_ITEMS = "^(\\d+) (?:(?:tin|tins) of soup|(?:loaf|loaves) of bread|" +
            "(?:bottle|bottles) of milk|(?:apple|apples))$";

    private Map<String, StockItem> shoppingBasket;

    private StockItem soup;
    private StockItem bread;
    private StockItem milk;
    private StockItem apples;

    private List<Discount> discounts;

    private LocalDate toBeBoughtOn;

    private BigDecimal totalWithNoDiscounts;
    private BigDecimal totalDiscount;

    public ShoppingSteps() {
        Given("the following available stock items", (DataTable dataTable) -> {
            List<List<String>> rows = dataTable.asLists(String.class);

            rows.stream().skip(1) /* The 1st row as it is just column headings */.forEach((List<String> columns) -> {
                System.out.println(columns);
                final var product = columns.get(0);
                final var cost = new BigDecimal(columns.get(2));
                switch (product) {
                    case "soup" -> {
                        soup = new StockItem(product, StockItemType.TIN, cost, 0);
                        // soup = Soup.of(cost);
                    }
                    case "bread" -> {
                        bread = new StockItem(product, StockItemType.LOAF, cost, 0);
                        // bread = Bread.of(cost);
                    }
                    case "milk" -> {
                        milk = new StockItem(product, StockItemType.BOTTLE, cost, 0);
                        // milk = Milk.of(cost);
                    }
                    case "apple" -> {
                        apples = new StockItem(product, StockItemType.SINGLE, cost, 0);
                        // apple = Apple.of(cost);
                    }
                    default -> {
                        throw new RuntimeException("Unsupported stock item type: " + columns.get(0));
                    }
                }
            });

            System.out.println(soup);
            System.out.println(bread);
            System.out.println(milk);
            System.out.println(apples);
        });

        And("^the following discounts$", (DataTable dataTable) -> {
            System.out.println(">>> the following discounts are available");

            List<List<String>> rows = dataTable.asLists(String.class);

            if (!rows.isEmpty()) {
                discounts = new ArrayList<>(rows.size());
                rows.stream().skip(1) /* The column headings */.forEach((List<String> columns) -> {
                    System.out.println(columns);
                    final var theOffer = columns.get(0);
                    final var validFrom = columns.get(1);
                    final var validTo = columns.get(2);

                    discounts.add(new Discount(theOffer, validFrom, validTo));
                });

                System.out.println(discounts);
            }
        });

        Given("an empty shopping basket", () -> {
            totalWithNoDiscounts = new BigDecimal("0.00");

            shoppingBasket = new HashMap<>();
            shoppingBasket.put("soup", soup);
            shoppingBasket.put("bread", bread);
            shoppingBasket.put("milk", milk);
            shoppingBasket.put("apples", apples);

            System.out.println(">>> Given an empty shopping basket!!!");
        });

        When("adding {string} to the shopping basket", (String items) -> {
            System.out.println(">>> item is " + items);
            var itemsForShoppingBasket = Arrays.stream(items.split(",")).map(String::trim).toList();
            System.out.println(">>> itemsForShoppingBasket = " + itemsForShoppingBasket);

//            Optional<Integer> optionalNumOfStockedItems = itemsForShoppingBasket.stream()
//                    .filter(x -> x.matches(NUMBER_OF_STOCK_ITEMS))
//                    .map(x -> {
//                        var pattern = Pattern.compile(NUMBER_OF_STOCK_ITEMS);
//                        var matcher = pattern.matcher(x);
//                        if (matcher.find()) {
//                            return Integer.parseInt(matcher.group(1));
//                        }
//                        return 0;
//                    })
//                    .findFirst();
//            optionalNumOfStockedItems.ifPresent(numOfItems -> System.out.println("\t Number of items is " +
//            numOfItems));

            itemsForShoppingBasket.forEach(requestedItem -> {
                if (requestedItem.matches(NUMBER_OF_STOCK_ITEMS)) {
                    var pattern = Pattern.compile(NUMBER_OF_STOCK_ITEMS);
                    var matcher = pattern.matcher(requestedItem);
                    if (matcher.find()) {
                        var requiredNumOfItems = Integer.parseInt(matcher.group(1));
                        if (requestedItem.contains("soup")) {
                            shoppingBasket.get("soup").requiredNumber = requiredNumOfItems;
                        } else if (requestedItem.contains("bread")) {
                            shoppingBasket.get("bread").requiredNumber = requiredNumOfItems;
                        } else if (requestedItem.contains("milk")) {
                            shoppingBasket.get("milk").requiredNumber = requiredNumOfItems;
                        } else if (requestedItem.contains("apple")) {
                            shoppingBasket.get("apples").requiredNumber = requiredNumOfItems;
                        }
                    }
                }
            });

            System.out.println("\t shoppingBasket has " + shoppingBasket);
        });

        And("is to be bought {string}", (String dateOfPurchase) -> {
            if (dateOfPurchase.equals("today")) {
                toBeBoughtOn = LocalDate.now();
                System.out.println(">>> to be bought today [" + toBeBoughtOn + "]");
            } else {
                var days = getDaysInAdvance(dateOfPurchase).orElse(3 /* default days */);
                toBeBoughtOn = LocalDate.now().plusDays(days);
                System.out.println(">>> to be bought in " + days + " days time on the date [" + toBeBoughtOn + "]");
            }

            // Total-up the basket...
            List<StockItem> stockItems = new ArrayList<>(shoppingBasket.values());
            stockItems.stream()
                    .filter(stockItem -> stockItem.requiredNumber != 0)
                    .forEach(stockItem -> {
                        BigDecimal subTotal = stockItem.cost.multiply(BigDecimal.valueOf(stockItem.requiredNumber));
                        totalWithNoDiscounts = totalWithNoDiscounts.add(subTotal);
                    });

            System.out.println(">>> Total without discount is " + totalWithNoDiscounts);
        });

        When("apply any discounts", () -> {
            System.out.println(">>> apply any discounts");

            totalDiscount = new BigDecimal("0.00");

            // Need to know what discounts are applicable and what is in the shopping basket
            discounts.forEach(currentDiscount -> {
                final var offer = currentDiscount.offer();

                if (isProductWithSomeDiscount(offer)) {
                    System.out.println(">>> FOUND DISCOUNT #1");
                    var optionalDiscountDetails = getProductAndPercentageValues(offer);
                    //optionalDiscountDetails.ifPresent(discountDetails -> System.out.println("\t" + discountDetails.getLeft() + ": " + discountDetails.getRight()));

                    if (optionalDiscountDetails.isPresent()) {
                        Pair<String, Integer> discountDetails = optionalDiscountDetails.get();

                        if (discountAvailableFromSpecifiedDaysHenceToEndOfMonth(3)) {
                            // Does the basket have some products, if so, apply a discount
                            StockItem stockItem = shoppingBasket.get(discountDetails.getLeft());

                            if (stockItem != null && stockItem.requiredNumber > 0) {
                                // Total-up number of apples and apply a 10% discount
                                final String meow = "0." + discountDetails.getRight();
                                BigDecimal subTotal = new BigDecimal(Integer.toString(stockItem.requiredNumber))
                                        .multiply(new BigDecimal(meow));

                                var discount = new BigDecimal("0.00");
                                discount = discount.add(subTotal.multiply(BigDecimal.valueOf(Double.parseDouble(meow))));

                                System.out.println("\t\tDISCOUNT IS " + discount);

                                totalDiscount = totalDiscount.add(discount).setScale(2, RoundingMode.HALF_EVEN);
                            }
                        }
                    }
                }

                if (isBuySomeProductsAndGetSomethingHalfPrice(offer)) {
                    System.out.println(">>> FOUND DISCOUNT #2");
                    var optionalDiscountDetails = getProductsAndSomethingForHalfPriceValues(offer);
                    //optionalDiscountDetails.ifPresent(discountDetails -> System.out.println("\t" + discountDetails.getLeft() + ": " + discountDetails.getRight()));

                    if (optionalDiscountDetails.isPresent()) {
                        Triple<Integer, String, String> discountDetails = optionalDiscountDetails.get();

                        if (discountAvailableFromYesterdayForSpecifiedDays(7)) {
                            StockItem stockItem = switch(discountDetails.getMiddle()) {
                                case "tin of soup", "tins of soup" -> shoppingBasket.get("soup");
                                case "loaf of bread", "loaves of bread" -> shoppingBasket.get("bread");
                                case "bottle of milk", "bottles of milk" -> shoppingBasket.get("milk");
                                case "apple", "apples" -> shoppingBasket.get("apples");
                                default -> null;
                            };

                            if (stockItem != null && stockItem.requiredNumber > 0) {
                                var halfPriceItem = switch(discountDetails.getRight()) {
                                    case "a tin of soup" -> shoppingBasket.get("soup").cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
                                    case "a loaf of bread" -> shoppingBasket.get("bread").cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
                                    case "a bottle of milk" -> shoppingBasket.get("milk").cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
                                    case "an apple" -> shoppingBasket.get("apples").cost.divide(BigDecimal.valueOf(2L), RoundingMode.HALF_UP);
                                    default -> new BigDecimal("0.00");
                                };

//                                BigDecimal subTotal =
//                                        halfPriceItem.multiply(new BigDecimal(Integer.toString(stockItem.requiredNumber)));
//
//                                var discount = new BigDecimal("0.00").add(subTotal);

                                System.out.println("\t\tDISCOUNT IS " + halfPriceItem);

                                totalDiscount = totalDiscount.add(halfPriceItem).setScale(2, RoundingMode.HALF_EVEN);
                            }
                        }
                    }
                }

                System.out.println();
            });
        });

        Then("the cost of the shopping is expected to be {bigdecimal}", (BigDecimal expectedTotalCost) -> {
            System.out.println(">>> price is " + expectedTotalCost);

            assertThat(totalWithNoDiscounts.subtract(totalDiscount), is(expectedTotalCost));
        });
    }

//    private void processDiscountOffers() {
//        System.out.println("HELLO!!!");
//        discounts.forEach(currentDiscount -> {
//            final String offer = currentDiscount.offer();
//
//            if (isProductWithSomePercentageDiscount(offer)) {
//                System.out.println(">>> FOUND DISCOUNT #1");
//                var optionalDiscountDetails = getProductAndPercentageValues(offer);
//                optionalDiscountDetails.ifPresent(discountDetails -> System.out.println("\t" + discountDetails
//                .getLeft() + ": " + discountDetails.getRight()));
//            }
//
//            if (isBuySomeProductsAndGetSomethingHalfPrice(offer)) {
//                System.out.println(">>> FOUND DISCOUNT #2");
//                var optionalDiscountDetails = getProductsAndSomethingForHalfPriceValues(offer);
//                optionalDiscountDetails.ifPresent(discountDetails -> System.out.println("\t" + discountDetails
//                .getLeft() + ": " + discountDetails.getRight()));
//            }
//        });
//    }

    private BigDecimal getDiscount() {
        var discount = new BigDecimal("0.00");

        /*
         * Apply the following discounts...
         * >>> Buy ??? and get a ??? half price
         * >>> ??? have a ???% discount
         */

        return discount;
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
     * @param offer
     * @return
     */
    private boolean isProductWithSomeDiscount(String offer) {
        return offer.matches(DISCOUNT_OFFER);
    }

    private Optional<Pair<String, Integer>> getProductAndPercentageValues(String offer) {
        Optional<Pair<String, Integer>> extractedDetails = Optional.empty();

        var pattern = Pattern.compile(DISCOUNT_OFFER);
        var matcher = pattern.matcher(offer);

        if (matcher.find()) {
            extractedDetails = Optional.of(new ImmutablePair<>(matcher.group(1), Integer.valueOf(matcher.group(2))));
        }

        return extractedDetails;
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
     * @param offer
     * @return
     */
    private boolean isBuySomeProductsAndGetSomethingHalfPrice(String offer) {
        return offer.matches(DISCOUNT_HALF_PRICE_OFFER);
    }

    private Optional<Triple<Integer, String, String>> getProductsAndSomethingForHalfPriceValues(String offer) {
        Optional<Triple<Integer, String, String>> details = Optional.empty();

        var pattern = Pattern.compile(DISCOUNT_HALF_PRICE_OFFER);
        var matcher = pattern.matcher(offer);

        if (matcher.find()) {
            details = Optional.of(ImmutableTriple.of(Integer.valueOf(matcher.group(1)), matcher.group(2), matcher.group(3)));
        }

        return details;
    }

    private boolean discountAvailableFromYesterdayForSpecifiedDays(final int specifiedDays) {
        LocalDate yesterday = toBeBoughtOn.minusDays(1);
        LocalDate forSpecifiedDays = toBeBoughtOn.plusDays(specifiedDays);

        return (!toBeBoughtOn.isBefore(yesterday) && (toBeBoughtOn.isBefore(forSpecifiedDays)));
    }

    private boolean discountAvailableFromSpecifiedDaysHenceToEndOfMonth(final int specifiedDays) {
        LocalDate today = LocalDate.now();
        LocalDate inSpecifiedDaysTime = today.plusDays(specifiedDays);
        LocalDate startDateOfCurrentMonth = toBeBoughtOn.withDayOfMonth(1);
        LocalDate endDateOfCurrentMonth = toBeBoughtOn.withDayOfMonth(startDateOfCurrentMonth.lengthOfMonth());

        boolean isAfterSpecifiedDays = toBeBoughtOn.isAfter(inSpecifiedDaysTime);
        boolean isBeforeOrOnTheEndOfTheMonth =
                toBeBoughtOn.isBefore(endDateOfCurrentMonth) || toBeBoughtOn.isEqual(endDateOfCurrentMonth);

        return isAfterSpecifiedDays && isBeforeOrOnTheEndOfTheMonth;
    }

    private Optional<Integer> getDaysInAdvance(String dateOfPurchase) {
        Optional<Integer> daysInAdvance = Optional.empty();

        var pattern = Pattern.compile("^in (\\d+) days time$");
        var matcher = pattern.matcher(dateOfPurchase);

        if (matcher.find()) {
            daysInAdvance = Optional.of(Integer.valueOf(matcher.group(1)));
        }

        return daysInAdvance;
    }
}
