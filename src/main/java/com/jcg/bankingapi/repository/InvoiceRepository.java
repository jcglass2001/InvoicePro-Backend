package com.jcg.bankingapi.repository;

import com.jcg.bankingapi.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
}
