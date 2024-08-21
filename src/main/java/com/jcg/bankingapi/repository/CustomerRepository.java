package com.jcg.bankingapi.repository;

import com.jcg.bankingapi.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Page<Customer> findByNameContaining(String name, Pageable pageable);
}
