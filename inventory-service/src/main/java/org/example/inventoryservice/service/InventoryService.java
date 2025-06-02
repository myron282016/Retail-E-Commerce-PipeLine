package org.example.inventoryservice.service;

import org.example.inventoryservice.model.InventoryOrder;
import org.example.inventoryservice.repository.InventoryOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {
    private final InventoryOrderRepository orderRepository;

    public InventoryService(InventoryOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


  public  InventoryOrder getProcessedOrder(String orderId) {

       return orderRepository.findById(orderId)
               .orElseThrow(() -> new RuntimeException("Order Not Found"));


   }


}
