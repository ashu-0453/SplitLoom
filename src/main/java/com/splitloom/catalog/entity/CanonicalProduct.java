package com.splitloom.catalog.entity;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "canonical_products")
public class CanonicalProduct {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String canonicalName;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double packageQuantity;

    @Column(nullable = false)
    private String unit;

    @Column(length = 1000)
    private String tags;

    // Vector embedding representation (384 dimensions)
    @Lob
    @Column(name = "embedding_data")
    private String embeddingJson;

    public CanonicalProduct() {
        this.id = UUID.randomUUID();
    }

    public CanonicalProduct(String canonicalName, String brand, String category,
                            Double packageQuantity, String unit, String tags) {
        this.id = UUID.randomUUID();
        this.canonicalName = canonicalName;
        this.brand = brand;
        this.category = category;
        this.packageQuantity = packageQuantity;
        this.unit = unit;
        this.tags = tags;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPackageQuantity() { return packageQuantity; }
    public void setPackageQuantity(Double packageQuantity) { this.packageQuantity = packageQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public float[] getEmbedding() {
        if (embeddingJson == null || embeddingJson.isBlank()) return new float[0];
        String[] parts = embeddingJson.replace("[", "").replace("]", "").split(",");
        float[] res = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            res[i] = Float.parseFloat(parts[i].trim());
        }
        return res;
    }

    public void setEmbedding(float[] embedding) {
        this.embeddingJson = Arrays.toString(embedding);
    }
}
