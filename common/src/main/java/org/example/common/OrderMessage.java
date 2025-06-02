package org.example.common;


import java.io.Serializable;

public class OrderMessage implements Serializable {
    private String orderId;
    private String item;

    // Constructors, getters, setters
    public OrderMessage() {}

    public OrderMessage(String orderId, String item) {
        this.orderId = orderId;
        this.item = item;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
}

