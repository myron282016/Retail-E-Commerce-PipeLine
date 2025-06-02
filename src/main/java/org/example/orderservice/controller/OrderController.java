package org.example.orderservice.controller;

import org.example.orderservice.model.OrderRequest;
import org.example.orderservice.service.OrderService;
import org.example.orderservice.transformer.OrderTransformerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderTransformerService transformerService;

    public OrderController(OrderService orderService, OrderTransformerService transformerService) {
        this.orderService = orderService;
        this.transformerService = transformerService;
        logger.info("OrderController initialized successfully");
    }

    @PostMapping(value = "/orders", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> placeOrder(
            @RequestBody String rawPayload,
            @RequestHeader("Content-Type") String contentType) {

        logger.info("Received order request: contentType={}, payloadLength={}",
                contentType, rawPayload != null ? rawPayload.length() : 0);

        try {
            logger.debug("Starting payload transformation for contentType: {}", contentType);
            OrderRequest orderRequest = transformerService.transform(rawPayload, contentType);

            logger.info("Successfully transformed payload to OrderRequest: orderId={}, orderType={}, storeId={}",
                    orderRequest.getOrderId(), orderRequest.getOrderType(), orderRequest.getStoreId());

            // Extra validation (optional)
            if (orderRequest.getOrderId() == null || orderRequest.getOrderType() == null) {
                logger.warn("Validation failed - missing required fields: orderId={}, orderType={}",
                        orderRequest.getOrderId(), orderRequest.getOrderType());
                return ResponseEntity.badRequest().body("Missing required fields: orderId or orderType");
            }

            logger.debug("Validation passed, publishing order to service: orderId={}", orderRequest.getOrderId());
            orderService.publishOrder(orderRequest);

            logger.info("Order successfully accepted and published: orderId={}", orderRequest.getOrderId());
            return ResponseEntity.ok("Order received");

        } catch (IOException e) {
            // Malformed JSON or XML
            logger.error("Failed to parse {} payload: error={}, payloadLength={}",
                    contentType, e.getMessage(), rawPayload != null ? rawPayload.length() : 0);
            return ResponseEntity
                    .badRequest()
                    .body("Invalid " + contentType + " payload: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            // Bad content-type or logic errors
            logger.error("Bad request received: contentType={}, error={}", contentType, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body("Bad request: " + e.getMessage());

        } catch (Exception e) {
            // Catch-all for unexpected failures
            logger.error("Unexpected error processing order request: contentType={}, error={}",
                    contentType, e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body("Unexpected server error: " + e.getMessage());
        }
    }
}