package com.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is insufficient stock for an operation.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableStock;

    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableStock) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d",
                productId, requestedQuantity, availableStock));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableStock = null;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }
}
