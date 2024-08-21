package com.jcg.bankingapi.service.implementation;

import com.jcg.bankingapi.domain.Customer;
import com.jcg.bankingapi.domain.Invoice;
import com.jcg.bankingapi.exception.ApiException;
import com.jcg.bankingapi.repository.CustomerRepository;
import com.jcg.bankingapi.repository.InvoiceRepository;
import com.jcg.bankingapi.service.InvoiceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.springframework.data.domain.PageRequest.of;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    @Override
    public Invoice createInvoice(Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        return invoiceRepository.save(invoice);
    }

    @Override
    public Page<Invoice> getInvoices(int page, int size) {
        return invoiceRepository.findAll(of(page,size));
    }

    @Override
    public void addInvoiceToCustomer(Long customerId, Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        Customer customer = customerRepository.findById(customerId).get();

        invoice.setCustomer(customer);
        invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ApiException(String.format("Invoice with id: %d not found", id)));
    }
}
