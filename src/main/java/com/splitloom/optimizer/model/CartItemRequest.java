package com.splitloom.optimizer.model;

public class CartItemRequest {
    private String name;
    private int quantity = 1;

    public CartItemRequest() {}

    public CartItemRequest(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
