package com.example.store.service;

import com.example.store.dto.CustomerCreateRequest;
import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerServiceTests {

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final CustomerService customerService = new CustomerService(customerRepository);

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Dr. Winifred Morissette");
    }

    @Test
    void getCustomers_returnsAllCustomers_whenQueryIsNull() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.getCustomers(null);

        assertThat(result).containsExactly(customer);
        verify(customerRepository, never()).findByNameWordContaining(anyString());
    }

    @Test
    void getCustomers_returnsAllCustomers_whenQueryIsBlank() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.getCustomers("   ");

        assertThat(result).containsExactly(customer);
        verify(customerRepository, never()).findByNameWordContaining(anyString());
    }

    @Test
    void getCustomers_delegatesToWordSearch_whenNameProvided() {
        when(customerRepository.findByNameWordContaining("isse")).thenReturn(List.of(customer));

        List<Customer> result = customerService.getCustomers("isse");

        assertThat(result).containsExactly(customer);
        verify(customerRepository).findByNameWordContaining("isse");
    }

    @Test
    void createCustomer_savesAndReturnsCustomer() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setName("Dr. Winifred Morissette");

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.createCustomer(request);

        assertThat(result).isEqualTo(customer);
        verify(customerRepository).save(any(Customer.class));
    }
}
