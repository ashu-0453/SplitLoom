package com.splitloom.geo.controller;

import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.service.DarkStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/geo")
@Tag(name = "Hyperlocal Geo Serviceability", description = "Redis Geospatial operations for dark store micro-serviceability")
public class GeoController {

    private final DarkStoreService darkStoreService;

    public GeoController(DarkStoreService darkStoreService) {
        this.darkStoreService = darkStoreService;
    }

    @PostMapping("/serviceability")
    @Operation(summary = "Find serviceable quick-commerce stores for user coordinates")
    public ResponseEntity<Map<String, Object>> checkServiceability(@RequestParam double lat,
                                                                   @RequestParam double lon,
                                                                   @RequestParam(defaultValue = "3.5") double radiusKm) {
        List<DarkStore> stores = darkStoreService.findServiceableStores(lat, lon, radiusKm);
        return ResponseEntity.ok(Map.of(
                "userLatitude", lat,
                "userLongitude", lon,
                "radiusKm", radiusKm,
                "serviceableStoresCount", stores.size(),
                "stores", stores
        ));
    }

    @PostMapping("/stores")
    @Operation(summary = "Register a new vendor dark store with GPS coordinates")
    public ResponseEntity<Map<String, String>> registerStore(@RequestBody DarkStore darkStore) {
        darkStoreService.registerDarkStore(darkStore);
        return ResponseEntity.ok(Map.of("message", "DarkStore successfully registered in Redis Geospatial Index", "storeId", darkStore.getStoreId()));
    }

    @GetMapping("/stores")
    @Operation(summary = "List all registered dark stores")
    public ResponseEntity<List<DarkStore>> listAllStores() {
        return ResponseEntity.ok(darkStoreService.getAllStores());
    }
}
