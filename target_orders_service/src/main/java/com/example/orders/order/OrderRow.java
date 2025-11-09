package com.example.orders.order;

public class OrderRow {
    public long id;
    public long userId;
    public String item;
    public int amount;

    public OrderRow(long id, long userId, String item, int amount) {
        this.id = id;
        this.userId = userId;
        this.item = item;
        this.amount = amount;
    }
}

