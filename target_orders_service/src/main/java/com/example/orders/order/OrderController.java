package com.example.orders.order;

import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {
    private final OrderService service;
    private final OrderRepo repo;

    public OrderController(OrderService service, OrderRepo repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostConstruct
    public void init() {
        repo.initSchema();
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders(@RequestParam("userId") long userId) {
        List<OrderRow> rows = service.listByUser(userId);
        Map<String, Object> out = new HashMap<>();
        out.put("userId", userId);
        out.put("count", rows.size());
        out.put("orders", rows);
        return ResponseEntity.ok(out);
    }
}
