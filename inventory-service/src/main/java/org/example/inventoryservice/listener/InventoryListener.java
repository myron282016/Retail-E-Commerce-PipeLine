package org.example.inventoryservice.listener;

import com.github.benmanes.caffeine.cache.Cache;

import org.example.inventoryservice.model.InventoryOrder;
import org.example.inventoryservice.repository.InventoryOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

@Component
public class InventoryListener {
    private static final Logger logger = LoggerFactory.getLogger(InventoryListener.class);
    private final Cache<String, Boolean> orderCache;
    private final InventoryOrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryListener(Cache<String, Boolean> orderCache, InventoryOrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderCache = orderCache;
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "orders-queue")
    public void processOrder(InventoryOrder order) {
        String orderId = order.getOrderId();

        logger.info("Received order for processing: orderId={}, orderType={}, storeId={}",
                orderId, order.getOrderType(), order.getStoreId());
        try {
            // Check cache for idempotency
            if (orderCache.getIfPresent(orderId) != null) {
                System.out.println("Duplicate order detected in cache, skipping: " + orderId);
                return;
            }

            // Check DB for idempotency
            if (orderRepository.existsById(orderId)) {
                System.out.println("Duplicate order detected in DB, skipping: " + orderId);
                orderCache.put(orderId, true);  // add to cache for faster lookup next time
                return;
            }
            // Process order
            logger.info("Processing new order: orderId={}", orderId);

            // Save processed order
            InventoryOrder inventoryOrder = new InventoryOrder();
            inventoryOrder.setOrderId(orderId);
            inventoryOrder.setOrderType(order.getOrderType());
            inventoryOrder.setStoreId(order.getStoreId());
            inventoryOrder.setProcessedAt(LocalDateTime.now());
            orderRepository.save(inventoryOrder);
            logger.debug("Order saved to database: orderId={}", orderId);
            // Add to cache
            orderCache.put(orderId, true);
            logger.debug("Order added to cache: orderId={}", orderId);
            // Update inventory logic here (simplified)
            logger.info("Inventory successfully updated for order: orderId={}", orderId);
        }
    catch (Exception e) {
        logger.error("Error processing order: orderId={}, error={}", orderId, e.getMessage(), e);
        sendToDeadLetterQueue(order, e);
    }
}

    private void sendToDeadLetterQueue(InventoryOrder order, Exception error) {
        try {
            logger.warn("Sending failed order to DLQ: orderId={}, reason={}",
                    order.getOrderId(), error.getMessage());

            // Create DLQ message with error details
            InventoryOrderDLQ dlqMessage = InventoryOrderDLQ.builder()
                    .originalOrder(order)
                    .errorMessage(error.getMessage())
                    .failedAt(LocalDateTime.now())
                    .retryCount(getRetryCount(order)) // Get from message headers if available
                    .build();

            // Send to DLQ using RabbitTemplate
            rabbitTemplate.convertAndSend("inventory-dlq-exchange", "dlq.inventory", dlqMessage);

            logger.info("Successfully sent order to DLQ: orderId={}", order.getOrderId());

        } catch (Exception dlqError) {
            logger.error("Failed to send message to DLQ: orderId={}, dlqError={}",
                    order.getOrderId(), dlqError.getMessage(), dlqError);
        }
    }
    private int getRetryCount(InventoryOrder order) {
        // You can get this from RabbitMQ message headers if configured
        // For now, return a default value
        return 3;
    }
}


