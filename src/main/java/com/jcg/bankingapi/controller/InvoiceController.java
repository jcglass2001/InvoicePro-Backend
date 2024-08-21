package com.jcg.bankingapi.controller;

import com.jcg.bankingapi.domain.Customer;
import com.jcg.bankingapi.domain.Invoice;
import com.jcg.bankingapi.domain.dto.UserDTO;
import com.jcg.bankingapi.domain.dto.response.HttpResponse;
import com.jcg.bankingapi.service.CustomerService;
import com.jcg.bankingapi.service.InvoiceService;
import com.jcg.bankingapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/invoice")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final CustomerService customerService;
    private final UserService userService;
    @PostMapping("/create")
    public ResponseEntity<HttpResponse> createInvoice(@AuthenticationPrincipal UserDTO user, @RequestBody Invoice invoice){
        return ResponseEntity.created(URI.create("")).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userService.getUserByEmail(user.getEmail()),
                                "invoices", invoiceService.createInvoice(invoice)
                        ))
                        .message("Invoice created")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build()
        );
    }
    @GetMapping("/new")
    public ResponseEntity<HttpResponse> newInvoice(@AuthenticationPrincipal UserDTO user){
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userService.getUserByEmail(user.getEmail()),
                                "customers", customerService.getCustomers()
                        ))
                        .message("Customers retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }
    @PostMapping("/addtocustomer/{id}")
    public ResponseEntity<HttpResponse> addInvoiceToCustomer(@AuthenticationPrincipal UserDTO user,
                                                             @PathVariable("id") Long id,
                                                             @RequestBody Invoice invoice){
        invoiceService.addInvoiceToCustomer(id,invoice);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userService.getUserByEmail(user.getEmail()),
                                "customers", customerService.getCustomers()
                        ))
                        .message(String.format("Invoice created for customer with ID: %d", id))
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<HttpResponse> getInvoices(@AuthenticationPrincipal UserDTO user,
                                                     @RequestParam Optional<Integer> page,
                                                     @RequestParam Optional<Integer> size){
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userService.getUserByEmail(user.getEmail()),
                                "page", invoiceService.getInvoices(
                                        page.orElse(0),
                                        size.orElse(2))
                        ))
                        .message("Invoices retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<HttpResponse> getInvoice(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id){
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userService.getUserByEmail(user.getEmail()),
                                "invoice", invoiceService.getInvoice(id),
                                "customer", invoiceService.getInvoice(id).getCustomer()
                        ))
                        .message("Invoice retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }
}
