package com.example.store.repository;

import com.example.store.entity.Order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = "customer")
    List<Order> findAll();
}
