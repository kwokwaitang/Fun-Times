package com.ford.stockitem;

import java.math.BigDecimal;

public class StockItem {

    private String product;

    private StockItemType unit;

    private BigDecimal cost;

    private Integer requiredNumber;

    public StockItem() {
    }

    public StockItem(String product, StockItemType unit, BigDecimal cost, Integer requiredNumber) {
        this.product = product;
        this.unit = unit;
        this.cost = cost;
        this.requiredNumber = requiredNumber;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public StockItemType getUnit() {
        return unit;
    }

    public void setUnit(StockItemType unit) {
        this.unit = unit;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getRequiredNumber() {
        return requiredNumber;
    }

    public void setRequiredNumber(Integer requiredNumber) {
        this.requiredNumber = requiredNumber;
    }

    @Override
    public String toString() {
        return "StockItem{" +
                "product='" + product + '\'' +
                ", unit=" + unit +
                ", cost=" + cost +
                ", requiredNumber=" + requiredNumber +
                '}';
    }
}
