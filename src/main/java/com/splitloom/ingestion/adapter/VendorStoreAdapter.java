package com.splitloom.ingestion.adapter;

import com.splitloom.catalog.entity.CanonicalProduct;
import com.splitloom.catalog.entity.StoreProductListing;
import com.splitloom.geo.model.DarkStore;
import com.splitloom.geo.model.VendorType;

public interface VendorStoreAdapter {
    VendorType getVendorType();
    StoreProductListing fetchListing(CanonicalProduct product, DarkStore store);
}
