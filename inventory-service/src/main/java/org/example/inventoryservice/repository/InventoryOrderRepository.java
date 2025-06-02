package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.InventoryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryOrderRepository extends JpaRepository<InventoryOrder, String> {
}
