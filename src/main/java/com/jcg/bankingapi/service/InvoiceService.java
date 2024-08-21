package com.jcg.bankingapi.service;

import com.jcg.bankingapi.domain.Invoice;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface InvoiceService {
    /**
     * Creates a new invoice.
     *
     * @param invoice The invoice to create.
     * @return The created invoice.
     */
    Invoice createInvoice(Invoice invoice);

    /**
     * Retrieves a page of invoices.
     *
     * @param page The page number (zero-based).
     * @param size The number of invoices per page.
     * @return A page of invoices.
     */
    Page<Invoice> getInvoices(int page, int size);

    /**
     * Adds an invoice to a customer.
     *
     * @param customerId The ID of the customer.
     * @param invoice The invoice to add.
     */
    void addInvoiceToCustomer(Long customerId, Invoice invoice);

    Invoice getInvoice(Long id);
}
