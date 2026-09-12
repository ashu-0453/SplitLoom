package com.splitloom.optimizer;

import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import com.splitloom.optimizer.model.StoreCartBreakdown;
import com.splitloom.optimizer.service.TotalLandedCostCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TotalLandedCostCalculatorTest {

    private final TotalLandedCostCalculator calculator = new TotalLandedCostCalculator();

    @Test
    void testLandedCostBelowFreeDeliveryThreshold() {
        DarkStore store = new DarkStore("z1", VendorType.ZEPTO, "Zepto Hub", 0, 0, 3,
                new BigDecimal("25.00"), new BigDecimal("199.00"), new BigDecimal("4.00"), BigDecimal.ZERO);

        StoreProductListing item = new StoreProductListing(UUID.randomUUID(), "Milk", VendorType.ZEPTO, "z1",
                "Amul Milk", new BigDecimal("27.00"), new BigDecimal("28.00"), true, new BigDecimal("27.00"), 1.0);

        // Buying 2 packets = ₹54 (below ₹199 threshold -> delivery fee applies)
        StoreCartBreakdown breakdown = calculator.calculateLandedCost(store, Map.of(item, 2));

        assertEquals(new BigDecimal("54.00"), breakdown.getItemsSubtotal());
        assertEquals(new BigDecimal("25.00"), breakdown.getDeliveryFee());
        assertEquals(new BigDecimal("4.00"), breakdown.getHandlingFee());
        // Landed total = 54 + 25 + 4 = 83.00
        assertEquals(new BigDecimal("83.00"), breakdown.getLandedTotal());
        assertEquals(new BigDecimal("145.00"), breakdown.getFreeDeliveryGap());
    }

    @Test
    void testLandedCostAboveFreeDeliveryThreshold() {
        DarkStore store = new DarkStore("z1", VendorType.ZEPTO, "Zepto Hub", 0, 0, 3,
                new BigDecimal("25.00"), new BigDecimal("199.00"), new BigDecimal("4.00"), BigDecimal.ZERO);

        StoreProductListing item = new StoreProductListing(UUID.randomUUID(), "Atta", VendorType.ZEPTO, "z1",
                "Aashirvaad Atta", new BigDecimal("265.00"), new BigDecimal("290.00"), true, new BigDecimal("265.00"), 1.0);

        // Buying 1 bag = ₹265 (above ₹199 threshold -> delivery fee waived!)
        StoreCartBreakdown breakdown = calculator.calculateLandedCost(store, Map.of(item, 1));

        assertEquals(new BigDecimal("265.00"), breakdown.getItemsSubtotal());
        assertEquals(BigDecimal.ZERO, breakdown.getDeliveryFee(), "Delivery fee should be waived above threshold");
        assertEquals(new BigDecimal("4.00"), breakdown.getHandlingFee());
        // Landed total = 265 + 4 = 269.00
        assertEquals(new BigDecimal("269.00"), breakdown.getLandedTotal());
        assertEquals(BigDecimal.ZERO, breakdown.getFreeDeliveryGap());
    }
}
