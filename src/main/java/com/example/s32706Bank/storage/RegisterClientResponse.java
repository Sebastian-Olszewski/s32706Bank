package com.example.s32706Bank.storage;

import java.math.BigDecimal;

public class RegisterClientResponse {
    private String clientId;
    private BigDecimal balance;

    public RegisterClientResponse() {
    }

    public RegisterClientResponse(String clientId, BigDecimal balance) {
        this.clientId = clientId;
        this.balance = balance;
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
}