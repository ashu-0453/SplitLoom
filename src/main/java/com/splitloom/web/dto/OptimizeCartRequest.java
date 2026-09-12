package com.splitloom.web.dto;

import com.splitloom.optimizer.model.CartItemRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OptimizeCartRequest {
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotEmpty(message = "Cart must contain at least one item")
    private List<CartItemRequest> items;

    public OptimizeCartRequest() {}

    public OptimizeCartRequest(Double latitude, Double longitude, List<CartItemRequest> items) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.items = items;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public List<CartItemRequest> getItems() { return items; }
    public void setItems(List<CartItemRequest> items) { this.items = items; }
}
