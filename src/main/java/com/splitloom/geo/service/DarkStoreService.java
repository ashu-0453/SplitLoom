package com.splitloom.geo.service;

import com.splitloom.geo.model.DarkStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DarkStoreService {

    private static final Logger log = LoggerFactory.getLogger(DarkStoreService.class);
    private static final String GEO_KEY = "splitloom:geo:darkstores";
    private static final String STORE_HASH_PREFIX = "splitloom:store:";

    private final RedisTemplate<String, Object> redisTemplate;
    // Local fallback store cache for resilience and offline mode
    private final Map<String, DarkStore> inMemoryStores = new ConcurrentHashMap<>();

    public DarkStoreService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void registerDarkStore(DarkStore store) {
        inMemoryStores.put(store.getStoreId(), store);
        try {
            // 1. Add coordinates to Redis Geospatial Set
            Point point = new Point(store.getLongitude(), store.getLatitude());
            redisTemplate.opsForGeo().add(GEO_KEY, point, store.getStoreId());

            // 2. Store detailed metadata in Redis
            redisTemplate.opsForValue().set(STORE_HASH_PREFIX + store.getStoreId(), store);
            log.info("Registered DarkStore {} ({}) in Redis Geospatial index", store.getName(), store.getVendor());
        } catch (Exception e) {
            log.warn("Redis unavailable during registerDarkStore, using in-memory store: {}", e.getMessage());
        }
    }

    public List<DarkStore> findServiceableStores(double userLat, double userLon, double maxRadiusKm) {
        List<DarkStore> serviceable = new ArrayList<>();
        try {
            Point userPoint = new Point(userLon, userLat);
            Distance radius = new Distance(maxRadiusKm, Metrics.KILOMETERS);
            Circle circle = new Circle(userPoint, radius);

            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .includeDistance()
                    .sortAscending();

            GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(GEO_KEY, circle, args);
            if (results != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<Object>> r : results) {
                    String storeId = r.getContent().getName().toString();
                    DarkStore store = (DarkStore) redisTemplate.opsForValue().get(STORE_HASH_PREFIX + storeId);
                    if (store == null) {
                        store = inMemoryStores.get(storeId);
                    }
                    if (store != null) {
                        serviceable.add(store);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Falling back to in-memory Haversine geospatial calculation: {}", e.getMessage());
            // In-memory fallback using Haversine formula
            for (DarkStore store : inMemoryStores.values()) {
                double dist = calculateHaversineDistance(userLat, userLon, store.getLatitude(), store.getLongitude());
                if (dist <= store.getDeliveryRadiusKm()) {
                    serviceable.add(store);
                }
            }
        }

        if (serviceable.isEmpty() && !inMemoryStores.isEmpty()) {
            serviceable.addAll(inMemoryStores.values());
        }

        return serviceable;
    }

    public List<DarkStore> getAllStores() {
        return new ArrayList<>(inMemoryStores.values());
    }

    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in KM
        double latDist = Math.toRadians(lat2 - lat1);
        double lonDist = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDist / 2) * Math.sin(latDist / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDist / 2) * Math.sin(lonDist / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
