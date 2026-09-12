package com.splitloom.optimizer;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import com.splitloom.optimizer.model.CartItemRequest;
import com.splitloom.optimizer.model.OptimizedBasketResult;
import com.splitloom.optimizer.service.SplitCartOptimizer;
import com.splitloom.optimizer.service.TotalLandedCostCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SplitCartOptimizerTest {

    private SplitCartOptimizer optimizer;
    private TotalLandedCostCalculator tlcCalculator;

    @BeforeEach
    void setup() {
        tlcCalculator = new TotalLandedCostCalculator();
        optimizer = new SplitCartOptimizer(tlcCalculator, 10.0);
    }

    @Test
    void testSingleStoreWinnerWhenNoSplitAdvantage() {
        // Setup: Zepto is cheaper on all items
        CanonicalProduct milk = new CanonicalProduct("Milk 500ml", "Amul", "Dairy", 500.0, "ML", "milk");
        CanonicalProduct bread = new CanonicalProduct("Bread", "Modern", "Bakery", 400.0, "G", "bread");

        DarkStore zeptoStore = new DarkStore("z1", VendorType.ZEPTO, "Zepto", 0, 0, 3, new BigDecimal("20"), new BigDecimal("150"), new BigDecimal("4"), BigDecimal.ZERO);
        DarkStore blinkitStore = new DarkStore("b1", VendorType.BLINKIT, "Blinkit", 0, 0, 3, new BigDecimal("25"), new BigDecimal("200"), new BigDecimal("5"), BigDecimal.ZERO);

        Map<VendorType, DarkStore> storeMap = Map.of(VendorType.ZEPTO, zeptoStore, VendorType.BLINKIT, blinkitStore);
        Map<String, CanonicalProduct> canonicalMap = Map.of("Milk", milk, "Bread", bread);

        List<StoreProductListing> zeptoListings = List.of(
                new StoreProductListing(milk.getId(), "Milk 500ml", VendorType.ZEPTO, "z1", "Zepto Milk", new BigDecimal("27"), new BigDecimal("28"), true, new BigDecimal("27"), 1.0),
                new StoreProductListing(bread.getId(), "Bread", VendorType.ZEPTO, "z1", "Zepto Bread", new BigDecimal("40"), new BigDecimal("45"), true, new BigDecimal("40"), 1.0)
        );

        List<StoreProductListing> blinkitListings = List.of(
                new StoreProductListing(milk.getId(), "Milk 500ml", VendorType.BLINKIT, "b1", "Blinkit Milk", new BigDecimal("28"), new BigDecimal("28"), true, new BigDecimal("28"), 1.0),
                new StoreProductListing(bread.getId(), "Bread", VendorType.BLINKIT, "b1", "Blinkit Bread", new BigDecimal("45"), new BigDecimal("45"), true, new BigDecimal("45"), 1.0)
        );

        Map<VendorType, List<StoreProductListing>> listings = Map.of(
                VendorType.ZEPTO, zeptoListings,
                VendorType.BLINKIT, blinkitListings
        );

        List<CartItemRequest> cart = List.of(new CartItemRequest("Milk", 1), new CartItemRequest("Bread", 1));

        OptimizedBasketResult result = optimizer.optimizeBasket(cart, canonicalMap, storeMap, listings);

        assertNotNull(result);
        assertEquals(VendorType.ZEPTO, result.getSingleStoreWinner().getVendor());
        assertFalse(result.isSplitRecommended(), "Split should not be recommended when Zepto is cheaper across all items");
    }

    @Test
    void testSplitOrderRecommendedWhenSubstantialPriceDifferenceOutweighsDeliveryFees() {
        // Setup: Milk is ₹27 on Zepto vs ₹35 on Blinkit.
        // But Atta 5kg is ₹220 on Blinkit vs ₹290 on Zepto!
        // The ₹70 Atta difference easily offsets an extra ₹20 delivery fee!
        CanonicalProduct milk = new CanonicalProduct("Milk 500ml", "Amul", "Dairy", 500.0, "ML", "milk");
        CanonicalProduct atta = new CanonicalProduct("Atta 5kg", "Aashirvaad", "Staples", 5.0, "KG", "atta");

        DarkStore zeptoStore = new DarkStore("z1", VendorType.ZEPTO, "Zepto", 0, 0, 3, new BigDecimal("20"), new BigDecimal("150"), new BigDecimal("4"), BigDecimal.ZERO);
        DarkStore blinkitStore = new DarkStore("b1", VendorType.BLINKIT, "Blinkit", 0, 0, 3, new BigDecimal("25"), new BigDecimal("200"), new BigDecimal("5"), BigDecimal.ZERO);

        Map<VendorType, DarkStore> storeMap = Map.of(VendorType.ZEPTO, zeptoStore, VendorType.BLINKIT, blinkitStore);
        Map<String, CanonicalProduct> canonicalMap = Map.of("Milk", milk, "Atta", atta);

        List<StoreProductListing> zeptoListings = List.of(
                new StoreProductListing(milk.getId(), "Milk 500ml", VendorType.ZEPTO, "z1", "Zepto Milk", new BigDecimal("27"), new BigDecimal("28"), true, new BigDecimal("27"), 1.0),
                new StoreProductListing(atta.getId(), "Atta 5kg", VendorType.ZEPTO, "z1", "Zepto Atta", new BigDecimal("290"), new BigDecimal("290"), true, new BigDecimal("290"), 1.0)
        );

        List<StoreProductListing> blinkitListings = List.of(
                new StoreProductListing(milk.getId(), "Milk 500ml", VendorType.BLINKIT, "b1", "Blinkit Milk", new BigDecimal("85"), new BigDecimal("85"), true, new BigDecimal("85"), 1.0),
                new StoreProductListing(atta.getId(), "Atta 5kg", VendorType.BLINKIT, "b1", "Blinkit Atta", new BigDecimal("200"), new BigDecimal("290"), true, new BigDecimal("200"), 1.0)
        );

        Map<VendorType, List<StoreProductListing>> listings = Map.of(
                VendorType.ZEPTO, zeptoListings,
                VendorType.BLINKIT, blinkitListings
        );

        List<CartItemRequest> cart = List.of(new CartItemRequest("Milk", 1), new CartItemRequest("Atta", 1));

        OptimizedBasketResult result = optimizer.optimizeBasket(cart, canonicalMap, storeMap, listings);

        assertNotNull(result);
        assertTrue(result.isSplitRecommended(), "Split should be recommended when high margin item has massive price discrepancy");
        assertEquals(2, result.getSplitStoreRecommendation().size());
        assertTrue(result.getNetSavingsInr().compareTo(BigDecimal.valueOf(10.0)) > 0);
    }
}
