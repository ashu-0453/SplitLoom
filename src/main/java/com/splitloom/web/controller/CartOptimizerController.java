package com.splitloom.web.controller;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.catalog.service.ProductCatalogService;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import com.splitloom.geo.service.DarkStoreService;
import com.splitloom.ingestion.dispatcher.ParallelVendorDispatcher;
import com.splitloom.optimizer.model.CartItemRequest;
import com.splitloom.optimizer.model.OptimizedBasketResult;
import com.splitloom.optimizer.service.SplitCartOptimizer;
import com.splitloom.web.dto.OptimizeCartRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Cart Optimizer & Catalog Search", description = "Core optimization endpoints for multi-store basket evaluation")
public class CartOptimizerController {

    private static final Logger log = LoggerFactory.getLogger(CartOptimizerController.class);

    private final DarkStoreService darkStoreService;
    private final ProductCatalogService catalogService;
    private final ParallelVendorDispatcher vendorDispatcher;
    private final SplitCartOptimizer cartOptimizer;

    public CartOptimizerController(DarkStoreService darkStoreService,
                                   ProductCatalogService catalogService,
                                   ParallelVendorDispatcher vendorDispatcher,
                                   SplitCartOptimizer cartOptimizer) {
        this.darkStoreService = darkStoreService;
        this.catalogService = catalogService;
        this.vendorDispatcher = vendorDispatcher;
        this.cartOptimizer = cartOptimizer;
    }

    @PostMapping("/cart/optimize")
    @Operation(summary = "Optimize user grocery basket across quick-commerce stores with split-order analysis")
    public ResponseEntity<OptimizedBasketResult> optimizeCart(@Valid @RequestBody OptimizeCartRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Find serviceable dark stores via Redis Geospatial
        List<DarkStore> serviceableStores = darkStoreService.findServiceableStores(request.getLatitude(), request.getLongitude(), 3.5);
        if (serviceableStores.isEmpty()) {
            OptimizedBasketResult emptyResult = new OptimizedBasketResult();
            emptyResult.setSummaryMessage("No quick-commerce dark stores currently service this location coordinates.");
            return ResponseEntity.ok(emptyResult);
        }

        // 2. Resolve items semantically via Spring AI / pgvector similarity
        Map<String, CanonicalProduct> canonicalMap = new HashMap<>();
        List<CanonicalProduct> matchedProducts = new ArrayList<>();

        for (CartItemRequest item : request.getItems()) {
            Optional<ProductCatalogService.CanonicalMatch> match = catalogService.findBestSemanticMatch(item.getName(), 0.40);
            if (match.isPresent()) {
                CanonicalProduct cp = match.get().getProduct();
                canonicalMap.put(item.getName(), cp);
                matchedProducts.add(cp);
            }
        }

        // 3. Dispatch parallel queries across stores using Java 21 Virtual Threads
        Map<VendorType, DarkStore> storeMap = new EnumMap<>(VendorType.class);
        for (DarkStore store : serviceableStores) {
            storeMap.putIfAbsent(store.getVendor(), store);
        }

        Map<VendorType, List<StoreProductListing>> listingsByVendor =
                vendorDispatcher.fetchAllListingsInParallel(matchedProducts, serviceableStores);

        // 4. Run Split-Cart Knapsack Optimization Algorithm
        OptimizedBasketResult result = cartOptimizer.optimizeBasket(request.getItems(), canonicalMap, storeMap, listingsByVendor);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Cart optimization completed in {}ms across {} stores with Virtual Threads", elapsed, storeMap.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/products/search")
    @Operation(summary = "Semantic product search with vector distance scoring")
    public ResponseEntity<List<Map<String, Object>>> searchProducts(@RequestParam String q) {
        List<CanonicalProduct> products = catalogService.getAllProducts();
        List<Map<String, Object>> results = new ArrayList<>();

        for (CanonicalProduct p : products) {
            Optional<ProductCatalogService.CanonicalMatch> match = catalogService.findBestSemanticMatch(q, 0.35);
            if (match.isPresent() && match.get().getProduct().getId().equals(p.getId())) {
                results.add(Map.of(
                        "id", p.getId(),
                        "name", p.getCanonicalName(),
                        "brand", p.getBrand(),
                        "category", p.getCategory(),
                        "similarityScore", match.get().getSimilarityScore()
                ));
            }
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/health")
    @Operation(summary = "System Health Check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "SplitLoom Engine",
                "virtualThreadsEnabled", true,
                "timestamp", System.currentTimeMillis()
        ));
    }
}
