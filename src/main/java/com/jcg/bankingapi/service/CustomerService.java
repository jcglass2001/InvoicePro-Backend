package com.jcg.bankingapi.service;

import com.jcg.bankingapi.domain.Customer;
import com.jcg.bankingapi.domain.Invoice;
import com.jcg.bankingapi.domain.Stats;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * Service interface for managing customers and invoices.
 */
public interface CustomerService {

    /**
     * Creates a new customer.
     *
     * @param customer The customer to create.
     * @return The created customer.
     */
    Customer createCustomer(Customer customer);

    /**
     * Updates an existing customer.
     *
     * @param customer The customer to update.
     * @return The updated customer.
     */
    Customer updateCustomer(Customer customer);

    /**
     * Retrieves a page of customers.
     *
     * @param page The page number (zero-based).
     * @param size The number of customers per page.
     * @return A page of customers.
     */
    Page<Customer> getCustomers(int page, int size);

    /**
     * Retrieves all customers.
     *
     * @return All customers.
     */
    Iterable<Customer> getCustomers();

    /**
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return The customer with the specified ID, or null if not found.
     */
    Customer getCustomer(Long id);

    /**
     * Searches for customers by name.
     *
     * @param name The name to search for.
     * @param page The page number (zero-based).
     * @param size The number of customers per page.
     * @return A page of customers matching the search criteria.
     */
    Page<Customer> searchCustomers(String name, int page, int size);

    Stats getStats();
}

