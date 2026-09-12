package com.splitloom.optimizer.service;

import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.optimizer.model.CartItemRequest;
import com.splitloom.optimizer.model.StoreCartBreakdown;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TotalLandedCostCalculator {

    /**
     * Calculates the true Total Landed Cost (TLC) at checkout:
     * Landed = Items Subtotal + Handling Fee + Surge Fee + (Subtotal >= FreeDeliveryThreshold ? 0 : DeliveryFee)
     */
    public StoreCartBreakdown calculateLandedCost(DarkStore store,
                                                 Map<StoreProductListing, Integer> itemQuantities) {
        StoreCartBreakdown breakdown = new StoreCartBreakdown(store.getVendor(), store.getName());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<StoreProductListing, Integer> entry : itemQuantities.entrySet()) {
            StoreProductListing listing = entry.getKey();
            int qty = entry.getValue();
            BigDecimal itemTotal = listing.getPrice().multiply(BigDecimal.valueOf(qty));
            subtotal = subtotal.add(itemTotal);

            breakdown.getItems().add(new StoreCartBreakdown.ItemFulfillment(
                    listing.getCanonicalName(),
                    listing.getVendorRawTitle(),
                    qty,
                    listing.getPrice(),
                    itemTotal
            ));
        }

        breakdown.setItemsSubtotal(subtotal);
        breakdown.setHandlingFee(store.getHandlingFee());
        breakdown.setSurgeFee(store.getSurgeFee());

        // Delivery Fee Waiver check
        BigDecimal deliveryFee;
        if (subtotal.compareTo(store.getFreeDeliveryThreshold()) >= 0) {
            deliveryFee = BigDecimal.ZERO;
            breakdown.setFreeDeliveryGap(BigDecimal.ZERO);
        } else {
            deliveryFee = store.getBaseDeliveryFee();
            breakdown.setFreeDeliveryGap(store.getFreeDeliveryThreshold().subtract(subtotal));
        }
        breakdown.setDeliveryFee(deliveryFee);

        BigDecimal total = subtotal
                .add(breakdown.getHandlingFee())
                .add(breakdown.getSurgeFee())
                .add(deliveryFee);
        breakdown.setLandedTotal(total);

        return breakdown;
    }
}
