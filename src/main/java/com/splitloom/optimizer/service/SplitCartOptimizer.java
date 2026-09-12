package com.splitloom.optimizer.service;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import com.splitloom.optimizer.model.CartItemRequest;
import com.splitloom.optimizer.model.OptimizedBasketResult;
import com.splitloom.optimizer.model.StoreCartBreakdown;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SplitCartOptimizer {

    private final TotalLandedCostCalculator landedCostCalculator;
    private final BigDecimal minSavingsToSplit;

    public SplitCartOptimizer(TotalLandedCostCalculator landedCostCalculator,
                              @Value("${splitloom.optimizer.min-savings-to-split-inr:15.0}") double minSavings) {
        this.landedCostCalculator = landedCostCalculator;
        this.minSavingsToSplit = BigDecimal.valueOf(minSavings);
    }

    public OptimizedBasketResult optimizeBasket(List<CartItemRequest> requests,
                                               Map<String, CanonicalProduct> canonicalMap,
                                               Map<VendorType, DarkStore> storeMap,
                                               Map<VendorType, List<StoreProductListing>> listingsByVendor) {
        OptimizedBasketResult result = new OptimizedBasketResult();

        // 1. Calculate Single-Store Fulfillments
        List<StoreCartBreakdown> singleStoreBreakdowns = new ArrayList<>();
        for (Map.Entry<VendorType, DarkStore> entry : storeMap.entrySet()) {
            VendorType vendor = entry.getKey();
            DarkStore store = entry.getValue();
            List<StoreProductListing> listings = listingsByVendor.getOrDefault(vendor, Collections.emptyList());

            Map<StoreProductListing, Integer> cart = matchCartToListings(requests, canonicalMap, listings);
            if (cart.size() == requests.size()) { // full fulfillment
                StoreCartBreakdown breakdown = landedCostCalculator.calculateLandedCost(store, cart);
                singleStoreBreakdowns.add(breakdown);
            }
        }

        if (singleStoreBreakdowns.isEmpty()) {
            result.setSummaryMessage("None of the available stores can fulfill the entire basket.");
            return result;
        }

        // Sort by lowest landed total
        singleStoreBreakdowns.sort(Comparator.comparing(StoreCartBreakdown::getLandedTotal));
        StoreCartBreakdown bestSingleStore = singleStoreBreakdowns.get(0);
        result.setSingleStoreWinner(bestSingleStore);
        result.setAllSingleStoreOptions(singleStoreBreakdowns);

        // 2. Evaluate Dual-Store Split Partitions (if 2 or more stores available and items >= 2)
        if (storeMap.size() >= 2 && requests.size() >= 2) {
            List<VendorType> vendors = new ArrayList<>(storeMap.keySet());
            BigDecimal bestSplitCost = bestSingleStore.getLandedTotal();
            List<StoreCartBreakdown> bestSplitBreakdown = null;

            int n = requests.size();
            int totalPartitions = 1 << n; // 2^n partitions

            for (int i = 0; i < vendors.size(); i++) {
                for (int j = i + 1; j < vendors.size(); j++) {
                    VendorType vA = vendors.get(i);
                    VendorType vB = vendors.get(j);
                    DarkStore sA = storeMap.get(vA);
                    DarkStore sB = storeMap.get(vB);
                    List<StoreProductListing> listA = listingsByVendor.getOrDefault(vA, Collections.emptyList());
                    List<StoreProductListing> listB = listingsByVendor.getOrDefault(vB, Collections.emptyList());

                    // Partition items: mask bit 0 -> Store A, bit 1 -> Store B
                    // skip 0 and 2^n - 1 (those are single store)
                    for (int mask = 1; mask < totalPartitions - 1; mask++) {
                        List<CartItemRequest> subsetA = new ArrayList<>();
                        List<CartItemRequest> subsetB = new ArrayList<>();

                        for (int bit = 0; bit < n; bit++) {
                            if ((mask & (1 << bit)) == 0) {
                                subsetA.add(requests.get(bit));
                            } else {
                                subsetB.add(requests.get(bit));
                            }
                        }

                        Map<StoreProductListing, Integer> cartA = matchCartToListings(subsetA, canonicalMap, listA);
                        Map<StoreProductListing, Integer> cartB = matchCartToListings(subsetB, canonicalMap, listB);

                        if (cartA.size() == subsetA.size() && cartB.size() == subsetB.size()) {
                            StoreCartBreakdown bA = landedCostCalculator.calculateLandedCost(sA, cartA);
                            StoreCartBreakdown bB = landedCostCalculator.calculateLandedCost(sB, cartB);
                            BigDecimal combinedLanded = bA.getLandedTotal().add(bB.getLandedTotal());

                            if (combinedLanded.compareTo(bestSplitCost) < 0) {
                                bestSplitCost = combinedLanded;
                                bestSplitBreakdown = List.of(bA, bB);
                            }
                        }
                    }
                }
            }

            BigDecimal savings = bestSingleStore.getLandedTotal().subtract(bestSplitCost);
            if (bestSplitBreakdown != null && savings.compareTo(minSavingsToSplit) >= 0) {
                result.setSplitRecommended(true);
                result.setSplitStoreRecommendation(bestSplitBreakdown);
                result.setNetSavingsInr(savings);
                result.setSummaryMessage(String.format("Split Order Recommended! You save ₹%s by splitting your cart between %s and %s (even after accounting for both delivery fees).",
                        savings.toPlainString(), bestSplitBreakdown.get(0).getVendor(), bestSplitBreakdown.get(1).getVendor()));
                return result;
            }
        }

        result.setSplitRecommended(false);
        result.setNetSavingsInr(BigDecimal.ZERO);
        result.setSummaryMessage(String.format("Single Store Recommended! %s is the cheapest complete fulfillment at ₹%s landed cost.",
                bestSingleStore.getVendor(), bestSingleStore.getLandedTotal().toPlainString()));
        return result;
    }

    private Map<StoreProductListing, Integer> matchCartToListings(List<CartItemRequest> requests,
                                                                 Map<String, CanonicalProduct> canonicalMap,
                                                                 List<StoreProductListing> listings) {
        Map<StoreProductListing, Integer> cart = new LinkedHashMap<>();
        for (CartItemRequest req : requests) {
            CanonicalProduct canonical = canonicalMap.get(req.getName());
            if (canonical != null) {
                for (StoreProductListing l : listings) {
                    if (l.getCanonicalProductId().equals(canonical.getId())) {
                        cart.put(l, req.getQuantity());
                        break;
                    }
                }
            }
        }
        return cart;
    }
}
