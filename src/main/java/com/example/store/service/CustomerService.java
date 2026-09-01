package com.example.store.service;

import com.example.store.dto.CustomerCreateRequest;
import com.example.store.entity.Customer;
import com.example.store.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getCustomers(String name) {
        if (!StringUtils.hasText(name)) {
            return customerRepository.findAll();
        }
        return customerRepository.findByNameWordContaining(name);
    }

    public Customer createCustomer(CustomerCreateRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        return customerRepository.save(customer);
    }
}
