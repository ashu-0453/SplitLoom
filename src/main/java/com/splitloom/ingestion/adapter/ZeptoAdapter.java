package com.splitloom.ingestion.adapter;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ZeptoAdapter implements VendorStoreAdapter {

    @Override
    public VendorType getVendorType() {
        return VendorType.ZEPTO;
    }

    @Override
    public StoreProductListing fetchListing(CanonicalProduct product, DarkStore store) {
        // Simulate network latency (Virtual Thread blocks gracefully without tying up OS thread)
        try {
            Thread.sleep(40);
        } catch (InterruptedException ignored) {}

        // Realistic Indian pricing matrix for Zepto
        BigDecimal mrp;
        BigDecimal price;
        String rawTitle;

        switch (product.getBrand().toLowerCase()) {
            case "amul":
                if (product.getCanonicalName().contains("Gold")) {
                    mrp = new BigDecimal("34.00");
                    price = new BigDecimal("33.00");
                    rawTitle = "Amul Gold Milk Pouch 500ml (Zepto)";
                } else if (product.getCanonicalName().contains("Butter")) {
                    mrp = new BigDecimal("275.00");
                    price = new BigDecimal("265.00");
                    rawTitle = "Amul Butter 500g Pouch";
                } else {
                    mrp = new BigDecimal("28.00");
                    price = new BigDecimal("27.00"); // Cheap milk on Zepto
                    rawTitle = "Amul Taaza Fresh Toned Milk Pouch 500 ml";
                }
                break;
            case "aashirvaad":
                mrp = new BigDecimal("290.00");
                price = new BigDecimal("265.00"); // Higher price on Atta
                rawTitle = "Aashirvaad Shudh Chakki Atta 5 kg";
                break;
            case "fortune":
                mrp = new BigDecimal("165.00");
                price = new BigDecimal("145.00");
                rawTitle = "Fortune Sunlite Refined Sunflower Oil Pouch 1L";
                break;
            case "eggoz":
                mrp = new BigDecimal("65.00");
                price = new BigDecimal("55.00");
                rawTitle = "Eggoz Farm Fresh White Eggs (Pack of 6)";
                break;
            default:
                mrp = new BigDecimal("50.00");
                price = new BigDecimal("45.00");
                rawTitle = product.getCanonicalName() + " (Zepto Express)";
                break;
        }

        BigDecimal unitPrice = price.divide(BigDecimal.valueOf(product.getPackageQuantity()), 2, RoundingMode.HALF_UP);
        return new StoreProductListing(product.getId(), product.getCanonicalName(), VendorType.ZEPTO,
                store.getStoreId(), rawTitle, price, mrp, true, unitPrice, 0.98);
    }
}
