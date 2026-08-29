package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(
            value = "SELECT DISTINCT c.* FROM customer c "
                    + "WHERE EXISTS ("
                    + "  SELECT 1 FROM unnest(string_to_array(c.name, ' ')) AS word "
                    + "  WHERE word ILIKE CONCAT('%', :name, '%')"
                    + ")",
            nativeQuery = true)
    List<Customer> findByNameWordContaining(@Param("name") String name);
}
