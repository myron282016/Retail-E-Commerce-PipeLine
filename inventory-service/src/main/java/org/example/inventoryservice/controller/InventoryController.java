package org.example.inventoryservice.controller;

import org.example.inventoryservice.model.InventoryOrder;;
import org.example.inventoryservice.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


    @RestController
    @RequestMapping("/inventory")
    public class InventoryController {

        // private final InventoryOrderRepository orderRepository;
        private final InventoryService inventoryService;

        public InventoryController(InventoryService inventoryService) {
            this.inventoryService = inventoryService;
        }


        @GetMapping("/orders/{orderId}")
        public ResponseEntity<?> getProcessedOrder(@PathVariable String orderId) {
            try {
                InventoryOrder inventoryOrder = inventoryService.getProcessedOrder(orderId);
                return ResponseEntity.ok(inventoryOrder);
            } catch (RuntimeException exception) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Order Not Found");
            }
        }
    }




