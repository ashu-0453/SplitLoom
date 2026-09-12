package com.splitloom.ingestion.adapter;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BlinkitAdapter implements VendorStoreAdapter {

    @Override
    public VendorType getVendorType() {
        return VendorType.BLINKIT;
    }

    @Override
    public StoreProductListing fetchListing(CanonicalProduct product, DarkStore store) {
        try {
            Thread.sleep(60);
        } catch (InterruptedException ignored) {}

        // Blinkit often has aggressive staple discounts (Atta / Oil), slightly higher dairy
        BigDecimal mrp;
        BigDecimal price;
        String rawTitle;

        switch (product.getBrand().toLowerCase()) {
            case "amul":
                if (product.getCanonicalName().contains("Gold")) {
                    mrp = new BigDecimal("34.00");
                    price = new BigDecimal("34.00");
                    rawTitle = "Amul Gold Full Cream Fresh Milk (500 ml)";
                } else if (product.getCanonicalName().contains("Butter")) {
                    mrp = new BigDecimal("275.00");
                    price = new BigDecimal("259.00");
                    rawTitle = "Amul Salted Butter 500g Block";
                } else {
                    mrp = new BigDecimal("28.00");
                    price = new BigDecimal("28.00"); // Milk at MRP
                    rawTitle = "Amul Taaza Toned Milk (500ml)";
                }
                break;
            case "aashirvaad":
                mrp = new BigDecimal("290.00");
                price = new BigDecimal("235.00"); // Much cheaper Atta on Blinkit! (Creates split cart opportunity)
                rawTitle = "Aashirvaad Superior MP Shudh Chakki Wheat Atta 5kg";
                break;
            case "fortune":
                mrp = new BigDecimal("165.00");
                price = new BigDecimal("138.00"); // Cheaper Oil on Blinkit!
                rawTitle = "Fortune Sunflower Refined Oil 1 L";
                break;
            case "eggoz":
                mrp = new BigDecimal("65.00");
                price = new BigDecimal("58.00");
                rawTitle = "Eggoz White Eggs Box 6 pcs";
                break;
            default:
                mrp = new BigDecimal("50.00");
                price = new BigDecimal("46.00");
                rawTitle = product.getCanonicalName() + " (Blinkit 10m)";
                break;
        }

        BigDecimal unitPrice = price.divide(BigDecimal.valueOf(product.getPackageQuantity()), 2, RoundingMode.HALF_UP);
        return new StoreProductListing(product.getId(), product.getCanonicalName(), VendorType.BLINKIT,
                store.getStoreId(), rawTitle, price, mrp, true, unitPrice, 0.97);
    }
}
