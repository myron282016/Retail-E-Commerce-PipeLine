package org.example.orderservice.service;



import lombok.RequiredArgsConstructor;
import org.example.orderservice.model.OrderRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange = "orders.exchange";
    private final String routingKey = "orders.key";

    public void publishOrder(OrderRequest request) {
        OrderRequest order = OrderRequest.builder()
                .orderId(request.getOrderId())
                .orderType(request.getOrderType())
                .storeId(request.getStoreId())
                .details(request.getDetails())
                .build();
        rabbitTemplate.convertAndSend(exchange, routingKey, order);
    }
}
