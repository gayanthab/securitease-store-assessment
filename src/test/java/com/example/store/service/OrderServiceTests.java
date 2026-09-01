package com.example.store.service;

import com.example.store.dto.OrderCreateRequest;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTests {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final OrderService orderService = new OrderService(orderRepository, customerRepository, productRepository);

    private Order order;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        product = new Product();
        product.setId(1L);
        product.setDescription("Test Product");

        order = new Order();
        order.setId(1L);
        order.setDescription("Test Order");
        order.setCustomer(customer);
        order.setProducts(List.of(product));
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
    void createOrder_savesAndReturnsOrder_whenValid() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setDescription("Test Order");
        request.setCustomerId(1L);
        request.setProductIds(List.of(1L));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(request);

        assertThat(result).isEqualTo(order);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_throwsIllegalArgumentException_whenProductIdsMissing() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setDescription("Test Order");
        request.setCustomerId(1L);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one product");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_throwsIllegalArgumentException_whenProductIdsEmpty() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setDescription("Test Order");
        request.setCustomerId(1L);
        request.setProductIds(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one product");
    }

    @Test
    void createOrder_throwsResourceNotFoundException_whenCustomerMissing() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setDescription("Test Order");
        request.setCustomerId(99L);
        request.setProductIds(List.of(1L));

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createOrder_throwsResourceNotFoundException_whenAnyProductMissing() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setDescription("Test Order");
        request.setCustomerId(1L);
        request.setProductIds(List.of(1L, 2L));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(product));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("product");
    }
}
