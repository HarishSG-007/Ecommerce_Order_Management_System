package com.harish.ecommerce;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public Order placeOrder(Long userId, List<OrderItem> items) {
        Order order = new Order();
        order.setUserId(userId);

        for (OrderItem oi : items) {
            Long prodId = oi.getProduct().getId();
            Product p = productRepository.findById(prodId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + prodId));

            oi.setProduct(p);
            oi.setOrder(order);
        }

        order.setItems(items);

        double total = items.stream()
                .mapToDouble(oi -> oi.getProduct().getPrice() * oi.getQuantity())
                .sum();

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
    public Order cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));


        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            System.out.println("Cancelling confirmed order...");
        }
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new RuntimeException("Cannot cancel a shipped order");
        }


        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);

    }
}