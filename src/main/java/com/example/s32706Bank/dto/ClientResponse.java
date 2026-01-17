package com.example.s32706Bank.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ClientResponse {
    private String clientId;
    private BigDecimal balance;
    private Instant createdAt;

    public ClientResponse() {
    }

    public ClientResponse(String clientId, BigDecimal balance, Instant createdAt) {
        this.clientId = clientId;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}