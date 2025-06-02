package org.example.inventoryservice.listener;

import lombok.Builder;
import lombok.Data;
import org.example.inventoryservice.model.InventoryOrder;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryOrderDLQ {
    private InventoryOrder originalOrder;
    private String errorMessage;
    private LocalDateTime failedAt;
    private int retryCount;
}
