package com.tradeops.controller;

import com.tradeops.model.request.CustomerLinkRequest;
import com.tradeops.service.CustomerLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storefront/customer-link")
@RequiredArgsConstructor
public class StoreFrontCustomerLinkController {

    private final CustomerLinkService customerLinkService;

    // FR-036: Сохранение минимальной связи покупателя с магазином
    @PostMapping
    public ResponseEntity<Void> createLink(@Valid @RequestBody CustomerLinkRequest request) {
        customerLinkService.createCustomerLink(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}