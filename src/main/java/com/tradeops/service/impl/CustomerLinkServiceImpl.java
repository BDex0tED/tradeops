package com.tradeops.service.impl;

import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.model.entity.CustomerLink;
import com.tradeops.model.entity.Trader;
import com.tradeops.model.request.CustomerLinkRequest;
import com.tradeops.repo.CustomerLinkRepo;
import com.tradeops.repo.TraderRepo;
import com.tradeops.service.CustomerLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class CustomerLinkServiceImpl implements CustomerLinkService {

    private final CustomerLinkRepo customerLinkRepo;
    private final TraderRepo traderRepo;

    @Override
    @Transactional
    public void createCustomerLink(CustomerLinkRequest request) {
        Trader trader = traderRepo.findById(request.traderId())
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found"));

        // Хэшируем телефон (FR-037)
        String contactHash = hashContact(request.customerPhone());

        if (customerLinkRepo.findByContactHash(contactHash).isEmpty()) {
            CustomerLink link = new CustomerLink();
            link.setTrader(trader);
            link.setContactHash(contactHash);
            link.setCustomerExternalId(request.customerExternalId());
            customerLinkRepo.save(link);
        }
    }

    private String hashContact(String contactInfo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(contactInfo.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash contact info", e);
        }
    }
}