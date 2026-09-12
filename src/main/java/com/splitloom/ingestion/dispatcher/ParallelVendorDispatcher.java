package com.splitloom.ingestion.dispatcher;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import com.splitloom.ingestion.adapter.VendorStoreAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class ParallelVendorDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ParallelVendorDispatcher.class);
    private final Map<VendorType, VendorStoreAdapter> adapters = new EnumMap<>(VendorType.class);

    public ParallelVendorDispatcher(List<VendorStoreAdapter> adapterList) {
        for (VendorStoreAdapter adapter : adapterList) {
            adapters.put(adapter.getVendorType(), adapter);
        }
    }

    /**
     * Uses Java 21 Project Loom Virtual Threads to query all serviceable dark stores in parallel.
     * High-throughput, non-blocking, sub-second latency.
     */
    public Map<VendorType, List<StoreProductListing>> fetchAllListingsInParallel(List<CanonicalProduct> products,
                                                                                List<DarkStore> serviceableStores) {
        Map<VendorType, List<StoreProductListing>> resultMap = new ConcurrentHashMap<>();

        // Group serviceable stores by vendor
        Map<VendorType, DarkStore> vendorStoreMap = new EnumMap<>(VendorType.class);
        for (DarkStore store : serviceableStores) {
            vendorStoreMap.putIfAbsent(store.getVendor(), store);
        }

        // Java 21 Virtual Thread Executor
        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Void>> tasks = new ArrayList<>();

            for (Map.Entry<VendorType, DarkStore> entry : vendorStoreMap.entrySet()) {
                VendorType vendor = entry.getKey();
                DarkStore store = entry.getValue();
                VendorStoreAdapter adapter = adapters.get(vendor);

                if (adapter != null) {
                    tasks.add(() -> {
                        List<StoreProductListing> listings = new ArrayList<>();
                        for (CanonicalProduct p : products) {
                            StoreProductListing listing = adapter.fetchListing(p, store);
                            if (listing != null) {
                                listings.add(listing);
                            }
                        }
                        resultMap.put(vendor, listings);
                        return null;
                    });
                }
            }

            // Execute all tasks in parallel on virtual threads with deadline
            virtualExecutor.invokeAll(tasks, 1500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Error executing parallel vendor dispatch via Virtual Threads: {}", e.getMessage());
        }

        return resultMap;
    }
}
