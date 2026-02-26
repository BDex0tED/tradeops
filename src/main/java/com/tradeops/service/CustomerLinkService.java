package com.tradeops.service;

import com.tradeops.model.request.CustomerLinkRequest;

public interface CustomerLinkService {
    void createCustomerLink(CustomerLinkRequest request);
}