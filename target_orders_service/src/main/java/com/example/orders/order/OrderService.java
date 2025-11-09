package com.example.orders.order;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepo repo;

    public OrderService(OrderRepo repo) { this.repo = repo; }

    public List<OrderRow> listByUser(long userId) {
        return repo.findByUser(userId);
    }
}

