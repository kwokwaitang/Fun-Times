package com.ford.till;

import java.math.BigDecimal;

public interface Till {
    /**
     * Price to pay by the customer (including any discounts on day of purchase)
     *
     * @return Price to pay
     */
    BigDecimal priceUp();

    /**
     * Total-up the basket (with no discounts)
     *
     * @return The total price of the goods in the basket
     */
    BigDecimal totalUp();
}
