package com.splitloom.geo.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DarkStore implements Serializable {
    private String storeId;
    private VendorType vendor;
    private String name;
    private double latitude;
    private double longitude;
    private double deliveryRadiusKm;
    private BigDecimal baseDeliveryFee;
    private BigDecimal freeDeliveryThreshold;
    private BigDecimal handlingFee;
    private BigDecimal surgeFee;

    public DarkStore() {}

    public DarkStore(String storeId, VendorType vendor, String name, double latitude, double longitude,
                     double deliveryRadiusKm, BigDecimal baseDeliveryFee, BigDecimal freeDeliveryThreshold,
                     BigDecimal handlingFee, BigDecimal surgeFee) {
        this.storeId = storeId;
        this.vendor = vendor;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.baseDeliveryFee = baseDeliveryFee;
        this.freeDeliveryThreshold = freeDeliveryThreshold;
        this.handlingFee = handlingFee;
        this.surgeFee = surgeFee;
    }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public VendorType getVendor() { return vendor; }
    public void setVendor(VendorType vendor) { this.vendor = vendor; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public void setDeliveryRadiusKm(double deliveryRadiusKm) { this.deliveryRadiusKm = deliveryRadiusKm; }

    public BigDecimal getBaseDeliveryFee() { return baseDeliveryFee; }
    public void setBaseDeliveryFee(BigDecimal baseDeliveryFee) { this.baseDeliveryFee = baseDeliveryFee; }

    public BigDecimal getFreeDeliveryThreshold() { return freeDeliveryThreshold; }
    public void setFreeDeliveryThreshold(BigDecimal freeDeliveryThreshold) { this.freeDeliveryThreshold = freeDeliveryThreshold; }

    public BigDecimal getHandlingFee() { return handlingFee; }
    public void setHandlingFee(BigDecimal handlingFee) { this.handlingFee = handlingFee; }

    public BigDecimal getSurgeFee() { return surgeFee; }
    public void setSurgeFee(BigDecimal surgeFee) { this.surgeFee = surgeFee; }
}
