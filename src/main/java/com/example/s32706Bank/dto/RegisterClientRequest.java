package com.example.s32706Bank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class RegisterClientRequest {
    @NotNull
    @PositiveOrZero
    private BigDecimal initialBalance;

    public RegisterClientRequest() {
    }

    public RegisterClientRequest(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}