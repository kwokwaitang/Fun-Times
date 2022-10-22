package com.ford;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.ford.HenryGroceryBasket.PURCHASE_IN_FIVE_DAYS_TIME;
import static com.ford.HenryGroceryBasket.PURCHASE_TODAY;

public class MainApp {
    public static void main(String[] args) {
        List<StockItem> basket = new ArrayList<>();

        Basket henryGroceryBasket;

        StockItem soupTins;
        StockItem loavesOfBread;
        StockItem apples;
        StockItem bottlesOfMilk;

        LocalDate dayOfPurchase = PURCHASE_IN_FIVE_DAYS_TIME;

        boolean continueShopping = false;

        Scanner in = new Scanner(System.in);

        do {
            System.out.print("Enter day of purchase (today or advance) ");
            String purchaseWhen = in.next();
            if (StringUtils.isEmpty(purchaseWhen) || purchaseWhen.equalsIgnoreCase("today")) {
                dayOfPurchase = PURCHASE_TODAY;
            }

            System.out.print("Number of soup tins ");
            int numOIfSoupTins = in.nextInt();
            if (numOIfSoupTins > 0) {
                soupTins = new StockItem("soup", StockItemType.TIN, BigDecimal.valueOf(0.65), numOIfSoupTins);
                basket.add(soupTins);
            }

            System.out.print("Number of loaves of bread ");
            int numOfLoavesOfBread = in.nextInt();
            if (numOfLoavesOfBread > 0) {
                loavesOfBread = new StockItem("bread", StockItemType.LOAF, BigDecimal.valueOf(0.80), numOfLoavesOfBread);
                basket.add(loavesOfBread);
            }

            System.out.print("Number of apples ");
            int numOfApples = in.nextInt();
            if (numOfApples > 0) {
                apples = new StockItem("apple", StockItemType.SINGLE, BigDecimal.valueOf(0.10), numOfApples);
                basket.add(apples);
            }

            System.out.print("Number of milk bottles ");
            int numOfMilkBottles = in.nextInt();
            if (numOfMilkBottles > 0) {
                bottlesOfMilk = new StockItem("milk", StockItemType.BOTTLE, BigDecimal.valueOf(1.30), numOfMilkBottles);
                basket.add(bottlesOfMilk);
            }

            henryGroceryBasket = new HenryGroceryBasket(basket, dayOfPurchase);

            BigDecimal grandTotal = henryGroceryBasket.priceUp();
            System.out.println(String.format("\nThe grand total is £%s", grandTotal));

            System.out.print("\nContinue (yes or no) ? ");
            String continueWithShopping = in.next();
            System.out.println(continueShopping + "\n\n");
            if (continueWithShopping.equalsIgnoreCase("yes")) {
                continueShopping = true;
                basket.clear();
            } else {
                continueShopping = false;
            }
        } while (continueShopping);
    }
}
