package com.splitloom.ingestion.adapter;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InstamartAdapter implements VendorStoreAdapter {

    @Override
    public VendorType getVendorType() {
        return VendorType.INSTAMART;
    }

    @Override
    public StoreProductListing fetchListing(CanonicalProduct product, DarkStore store) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}

        BigDecimal mrp;
        BigDecimal price;
        String rawTitle;

        switch (product.getBrand().toLowerCase()) {
            case "amul":
                if (product.getCanonicalName().contains("Gold")) {
                    mrp = new BigDecimal("34.00");
                    price = new BigDecimal("33.50");
                    rawTitle = "Swiggy Instamart - Amul Gold Milk 500ml";
                } else if (product.getCanonicalName().contains("Butter")) {
                    mrp = new BigDecimal("275.00");
                    price = new BigDecimal("269.00");
                    rawTitle = "Amul Butter 500 gm";
                } else {
                    mrp = new BigDecimal("28.00");
                    price = new BigDecimal("27.50");
                    rawTitle = "Amul Toned Milk Pouch 0.5 L";
                }
                break;
            case "aashirvaad":
                mrp = new BigDecimal("290.00");
                price = new BigDecimal("255.00");
                rawTitle = "Aashirvaad Whole Wheat Atta 5kg Pouch";
                break;
            case "fortune":
                mrp = new BigDecimal("165.00");
                price = new BigDecimal("142.00");
                rawTitle = "Fortune Refined Sunflower Cooking Oil 1 Litre";
                break;
            case "eggoz":
                mrp = new BigDecimal("65.00");
                price = new BigDecimal("54.00");
                rawTitle = "Eggoz Farm Fresh Eggs - Pack of 6";
                break;
            default:
                mrp = new BigDecimal("50.00");
                price = new BigDecimal("44.00");
                rawTitle = product.getCanonicalName() + " (Instamart)";
                break;
        }

        BigDecimal unitPrice = price.divide(BigDecimal.valueOf(product.getPackageQuantity()), 2, RoundingMode.HALF_UP);
        return new StoreProductListing(product.getId(), product.getCanonicalName(), VendorType.INSTAMART,
                store.getStoreId(), rawTitle, price, mrp, true, unitPrice, 0.96);
    }
}
