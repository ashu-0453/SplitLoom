package com.splitloom;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.repository.CanonicalProductRepository;
import com.splitloom.catalog.service.VectorEmbeddingService;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;
import com.splitloom.geo.service.DarkStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SplitLoomApplication {

    private static final Logger log = LoggerFactory.getLogger(SplitLoomApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SplitLoomApplication.class, args);
    }

    @Bean
    CommandLineRunner seedDemoData(DarkStoreService darkStoreService,
                                  CanonicalProductRepository productRepo,
                                  VectorEmbeddingService embeddingService) {
        return args -> {
            log.info("Initializing SplitLoom sample Bangalore Dark Stores and Canonical Catalog...");

            // 1. Seed Dark Stores for Koramangala / Indiranagar / HSR (Bangalore)
            // User sample location: 12.9352, 77.6245 (Koramangala 4th Block)
            darkStoreService.registerDarkStore(new DarkStore(
                    "zepto-koramangala", VendorType.ZEPTO, "Zepto Koramangala DarkStore #104",
                    12.9345, 77.6230, 3.5,
                    new BigDecimal("25.00"), new BigDecimal("199.00"), new BigDecimal("4.00"), BigDecimal.ZERO
            ));

            darkStoreService.registerDarkStore(new DarkStore(
                    "blinkit-koramangala", VendorType.BLINKIT, "Blinkit Koramangala Hub #42",
                    12.9360, 77.6255, 4.0,
                    new BigDecimal("30.00"), new BigDecimal("249.00"), new BigDecimal("5.00"), BigDecimal.ZERO
            ));

            darkStoreService.registerDarkStore(new DarkStore(
                    "instamart-koramangala", VendorType.INSTAMART, "Swiggy Instamart Pod #88",
                    12.9330, 77.6210, 3.0,
                    new BigDecimal("20.00"), new BigDecimal("199.00"), new BigDecimal("4.50"), BigDecimal.ZERO
            ));

            log.info("Seeded 3 quick-commerce dark stores in Redis Geospatial Index.");

            // 2. Seed Canonical Products if empty
            if (productRepo.count() == 0) {
                List<CanonicalProduct> products = List.of(
                        createProduct("Amul Taaza Toned Fresh Milk", "Amul", "Dairy", 500.0, "ML",
                                "milk, amul, taaza, toned, fresh, dairy, pouch", embeddingService),
                        createProduct("Amul Gold Full Cream Milk", "Amul", "Dairy", 500.0, "ML",
                                "milk, amul, gold, full cream, fresh, dairy", embeddingService),
                        createProduct("Aashirvaad Superior MP Shudh Chakki Atta", "Aashirvaad", "Staples", 5.0, "KG",
                                "atta, aashirvaad, wheat, flour, chakki, shudh, 5kg", embeddingService),
                        createProduct("Fortune Sunlite Refined Sunflower Oil", "Fortune", "Cooking Oils", 1.0, "L",
                                "oil, sunflower oil, fortune, refined, cooking oil, 1l", embeddingService),
                        createProduct("Eggoz Farm Fresh White Eggs", "Eggoz", "Dairy & Eggs", 6.0, "COUNT",
                                "eggs, egg, eggoz, fresh eggs, white eggs, 6 pack", embeddingService),
                        createProduct("Modern Classic White Bread", "Modern", "Bakery", 400.0, "G",
                                "bread, white bread, modern, sandwich bread, bakery", embeddingService),
                        createProduct("Nescafe Classic Instant Coffee", "Nescafe", "Beverages", 100.0, "G",
                                "coffee, nescafe, classic, instant coffee, jar", embeddingService),
                        createProduct("Amul Pasteurised Butter", "Amul", "Dairy", 500.0, "G",
                                "butter, amul butter, pasteurised, salted, dairy", embeddingService)
                );
                productRepo.saveAll(products);
                log.info("Seeded {} canonical products with vector embeddings in PostgreSQL.", products.size());
            }
        };
    }

    private CanonicalProduct createProduct(String name, String brand, String category,
                                          Double quantity, String unit, String tags,
                                          VectorEmbeddingService embeddingService) {
        CanonicalProduct p = new CanonicalProduct(name, brand, category, quantity, unit, tags);
        p.setEmbedding(embeddingService.generateEmbedding(name + " " + brand + " " + tags));
        return p;
    }
}
