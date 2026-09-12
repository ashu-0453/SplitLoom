package com.splitloom.catalog.entity;

import com.splitloom.geo.model.VendorType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class StoreProductListing implements Serializable {
    private UUID canonicalProductId;
    private String canonicalName;
    private VendorType vendor;
    private String storeId;
    private String vendorRawTitle;
    private BigDecimal price;
    private BigDecimal mrp;
    private boolean inStock;
    private BigDecimal normalizedUnitPrice; // e.g. Price per 100g / 100ml / piece
    private double similarityScore; // Cosine similarity to query

    public StoreProductListing() {}

    public StoreProductListing(UUID canonicalProductId, String canonicalName, VendorType vendor,
                               String storeId, String vendorRawTitle, BigDecimal price, BigDecimal mrp,
                               boolean inStock, BigDecimal normalizedUnitPrice, double similarityScore) {
        this.canonicalProductId = canonicalProductId;
        this.canonicalName = canonicalName;
        this.vendor = vendor;
        this.storeId = storeId;
        this.vendorRawTitle = vendorRawTitle;
        this.price = price;
        this.mrp = mrp;
        this.inStock = inStock;
        this.normalizedUnitPrice = normalizedUnitPrice;
        this.similarityScore = similarityScore;
    }

    public UUID getCanonicalProductId() { return canonicalProductId; }
    public void setCanonicalProductId(UUID canonicalProductId) { this.canonicalProductId = canonicalProductId; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public VendorType getVendor() { return vendor; }
    public void setVendor(VendorType vendor) { this.vendor = vendor; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getVendorRawTitle() { return vendorRawTitle; }
    public void setVendorRawTitle(String vendorRawTitle) { this.vendorRawTitle = vendorRawTitle; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public BigDecimal getNormalizedUnitPrice() { return normalizedUnitPrice; }
    public void setNormalizedUnitPrice(BigDecimal normalizedUnitPrice) { this.normalizedUnitPrice = normalizedUnitPrice; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
}
