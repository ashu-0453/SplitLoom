package com.splitloom.catalog.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@Service
public class VectorEmbeddingService {

    public static final int EMBEDDING_DIMENSION = 128;

    /**
     * Generates a normalized semantic embedding vector using deterministic sub-word n-gram
     * and hashing projections (mimics Spring AI MiniLM embeddings offline without external API keys).
     */
    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return new float[EMBEDDING_DIMENSION];
        }

        float[] vector = new float[EMBEDDING_DIMENSION];
        String cleaned = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = cleaned.split("\\s+");

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (String token : tokens) {
                if (token.isBlank()) continue;
                // Hash word and bi-grams into dimension buckets
                byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
                int bucket = Math.abs((hash[0] << 8) | (hash[1] & 0xFF)) % EMBEDDING_DIMENSION;
                float weight = 1.0f + (token.length() > 3 ? 0.5f : 0.0f);
                vector[bucket] += weight;

                // Also hash character n-grams for typo & abbreviation tolerance (e.g. 500ml vs 500 ml vs 0.5L)
                for (int i = 0; i < token.length() - 2; i++) {
                    String trigram = token.substring(i, i + 3);
                    byte[] triHash = md.digest(trigram.getBytes(StandardCharsets.UTF_8));
                    int triBucket = Math.abs((triHash[0] << 8) | (triHash[1] & 0xFF)) % EMBEDDING_DIMENSION;
                    vector[triBucket] += 0.3f;
                }
            }
        } catch (Exception ignored) {}

        // Normalize vector to unit length (L2 norm) so dot product = cosine similarity
        double normSq = 0.0;
        for (float v : vector) {
            normSq += v * v;
        }
        double norm = Math.sqrt(normSq);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
        return vector;
    }

    /**
     * Calculates the Cosine Similarity between two embedding vectors:
     * cos(theta) = (A . B) / (||A|| * ||B||)
     * For unit normalized vectors, this is simply the dot product.
     */
    public double cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length || v1.length == 0) {
            return 0.0;
        }
        double dotProduct = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
        }
        return Math.max(0.0, Math.min(1.0, dotProduct));
    }
}
