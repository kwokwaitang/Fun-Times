package com.ford.basket

import java.math.BigDecimal

interface Basket {
    /**
     * Total-up the goods in the basket and apply any discounts, if applicable
     *
     * @return The price that the customer must pay
     */
    fun priceUp(): BigDecimal?
}