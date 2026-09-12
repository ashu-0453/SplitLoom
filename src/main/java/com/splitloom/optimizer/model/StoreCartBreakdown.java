package com.splitloom.optimizer.model;

import com.splitloom.geo.model.VendorType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StoreCartBreakdown {
    private VendorType vendor;
    private String storeName;
    private List<ItemFulfillment> items = new ArrayList<>();
    private BigDecimal itemsSubtotal = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal handlingFee = BigDecimal.ZERO;
    private BigDecimal surgeFee = BigDecimal.ZERO;
    private BigDecimal landedTotal = BigDecimal.ZERO;
    private BigDecimal freeDeliveryGap = BigDecimal.ZERO;

    public StoreCartBreakdown() {}

    public StoreCartBreakdown(VendorType vendor, String storeName) {
        this.vendor = vendor;
        this.storeName = storeName;
    }

    public static class ItemFulfillment {
        private String canonicalName;
        private String vendorTitle;
        private int quantity;
        private BigDecimal itemPrice;
        private BigDecimal totalCost;

        public ItemFulfillment(String canonicalName, String vendorTitle, int quantity, BigDecimal itemPrice, BigDecimal totalCost) {
            this.canonicalName = canonicalName;
            this.vendorTitle = vendorTitle;
            this.quantity = quantity;
            this.itemPrice = itemPrice;
            this.totalCost = totalCost;
        }

        public String getCanonicalName() { return canonicalName; }
        public String getVendorTitle() { return vendorTitle; }
        public int getQuantity() { return quantity; }
        public BigDecimal getItemPrice() { return itemPrice; }
        public BigDecimal getTotalCost() { return totalCost; }
    }

    public VendorType getVendor() { return vendor; }
    public void setVendor(VendorType vendor) { this.vendor = vendor; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public List<ItemFulfillment> getItems() { return items; }
    public void setItems(List<ItemFulfillment> items) { this.items = items; }

    public BigDecimal getItemsSubtotal() { return itemsSubtotal; }
    public void setItemsSubtotal(BigDecimal itemsSubtotal) { this.itemsSubtotal = itemsSubtotal; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getHandlingFee() { return handlingFee; }
    public void setHandlingFee(BigDecimal handlingFee) { this.handlingFee = handlingFee; }

    public BigDecimal getSurgeFee() { return surgeFee; }
    public void setSurgeFee(BigDecimal surgeFee) { this.surgeFee = surgeFee; }

    public BigDecimal getLandedTotal() { return landedTotal; }
    public void setLandedTotal(BigDecimal landedTotal) { this.landedTotal = landedTotal; }

    public BigDecimal getFreeDeliveryGap() { return freeDeliveryGap; }
    public void setFreeDeliveryGap(BigDecimal freeDeliveryGap) { this.freeDeliveryGap = freeDeliveryGap; }
}
