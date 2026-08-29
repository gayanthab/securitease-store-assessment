package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTests {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderService orderService = new OrderService(orderRepository);

    private Order order;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        order = new Order();
        order.setId(1L);
        order.setDescription("Test Order");
        order.setCustomer(customer);
    }

    @Test
    void getAllOrders_returnsAllOrdersFromRepository() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).containsExactly(order);
    }

    @Test
    void getOrderById_returnsOrder_whenFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result).isEqualTo(order);
    }

    @Test
    void getOrderById_throwsResourceNotFoundException_whenMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createOrder_savesAndReturnsOrder() {
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertThat(result).isEqualTo(order);
        verify(orderRepository).save(order);
    }
}
