package com.splitloom.catalog.service;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.repository.CanonicalProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductCatalogService {

    private final CanonicalProductRepository productRepository;
    private final VectorEmbeddingService embeddingService;

    public ProductCatalogService(CanonicalProductRepository productRepository,
                                 VectorEmbeddingService embeddingService) {
        this.productRepository = productRepository;
        this.embeddingService = embeddingService;
    }

    public List<CanonicalProduct> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Resolves a raw search query (or disparate vendor SKU title) to the closest canonical product
     * using semantic vector cosine similarity.
     */
    public Optional<CanonicalMatch> findBestSemanticMatch(String rawQuery, double minThreshold) {
        List<CanonicalProduct> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) return Optional.empty();

        float[] queryEmbedding = embeddingService.generateEmbedding(rawQuery);

        CanonicalProduct bestProduct = null;
        double bestScore = -1.0;

        for (CanonicalProduct p : allProducts) {
            float[] productEmbedding = p.getEmbedding();
            double similarity = embeddingService.cosineSimilarity(queryEmbedding, productEmbedding);
            if (similarity > bestScore) {
                bestScore = similarity;
                bestProduct = p;
            }
        }

        if (bestProduct != null && bestScore >= minThreshold) {
            return Optional.of(new CanonicalMatch(bestProduct, bestScore));
        }
        return Optional.empty();
    }

    public static class CanonicalMatch {
        private final CanonicalProduct product;
        private final double similarityScore;

        public CanonicalMatch(CanonicalProduct product, double similarityScore) {
            this.product = product;
            this.similarityScore = similarityScore;
        }

        public CanonicalProduct getProduct() { return product; }
        public double getSimilarityScore() { return similarityScore; }
    }
}
