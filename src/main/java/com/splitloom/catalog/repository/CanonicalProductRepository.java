package com.splitloom.catalog.repository;

import com.splitloom.catalog.entity.CanonicalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CanonicalProductRepository extends JpaRepository<CanonicalProduct, UUID> {
    Optional<CanonicalProduct> findByCanonicalNameIgnoreCase(String canonicalName);
}
