package com.ford;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ShoppingSteps implements En {

    private final Shopping shopping = new Shopping();

    public ShoppingSteps() {
        Given("the following available stock items", (DataTable dataTable) -> {
            shopping.setupStockItems(dataTable.asLists(String.class));
        });

        And("^the following discounts$", (DataTable dataTable) -> {
            shopping.setupDiscounts(dataTable.asLists(String.class));
        });

        Given("an empty shopping basket", shopping::emptyShoppingBasket);

        When("adding {string} to the shopping basket", (String items) -> {
            var stockItemsForShoppingBasket = Arrays.stream(items.split(",")).map(String::trim).toList();
            stockItemsForShoppingBasket.forEach(shopping::updateShoppingBasketWithRequiredNumber);
        });

        And("is to be bought {string}", (String dateOfPurchase) -> {
            shopping.determineWhenToBeBought(dateOfPurchase);
            shopping.totalUpShoppingBasket();
        });

        When("apply any discounts", shopping::applyDiscount);

        Then("the cost of the shopping is expected to be {bigdecimal}", (BigDecimal totalCost) -> {
            assertThat(shopping.getTotalWithNoDiscounts().subtract(shopping.getTotalDiscount()), is(totalCost));
        });
    }
}
