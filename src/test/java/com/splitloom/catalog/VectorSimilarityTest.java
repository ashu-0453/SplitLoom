package com.splitloom.catalog;

import com.splitloom.catalog.service.VectorEmbeddingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VectorSimilarityTest {

    private final VectorEmbeddingService embeddingService = new VectorEmbeddingService();

    @Test
    void testIdenticalStringsProducePerfectCosineSimilarity() {
        String text = "Amul Taaza Toned Milk 500ml";
        float[] v1 = embeddingService.generateEmbedding(text);
        float[] v2 = embeddingService.generateEmbedding(text);

        double similarity = embeddingService.cosineSimilarity(v1, v2);
        assertEquals(1.0, similarity, 0.001, "Identical text should have cosine similarity of 1.0");
    }

    @Test
    void testDisparateVendorTitlesHaveHighScore() {
        String titleZepto = "Amul Taaza Fresh Toned Milk Pouch 500 ml";
        String titleBlinkit = "Amul Taaza Toned Milk (500ml)";

        float[] v1 = embeddingService.generateEmbedding(titleZepto);
        float[] v2 = embeddingService.generateEmbedding(titleBlinkit);

        double similarity = embeddingService.cosineSimilarity(v1, v2);
        assertTrue(similarity > 0.65, "Similar product titles with different formats should score high similarity");
    }

    @Test
    void testUnrelatedProductsHaveLowScore() {
        String milk = "Amul Taaza Toned Fresh Milk 500ml";
        String detergent = "Surf Excel Quick Wash Detergent Powder 1kg";

        float[] v1 = embeddingService.generateEmbedding(milk);
        float[] v2 = embeddingService.generateEmbedding(detergent);

        double similarity = embeddingService.cosineSimilarity(v1, v2);
        assertTrue(similarity < 0.30, "Completely different categories should have low similarity");
    }
}
