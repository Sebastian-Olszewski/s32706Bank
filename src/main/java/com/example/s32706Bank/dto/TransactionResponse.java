package com.example.s32706Bank.dto;

import com.example.s32706Bank.model.TransactionCode;
import com.example.s32706Bank.model.TransactionStatus;

import java.math.BigDecimal;

public class TransactionResponse {
    private TransactionStatus status;
    private TransactionCode code;
    private String message;
    private BigDecimal newBalance;

    public TransactionResponse() {
    }

    public TransactionResponse(TransactionStatus status, TransactionCode code, String message, BigDecimal newBalance) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.newBalance = newBalance;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public TransactionCode getCode() {
        return code;
    }

    public void setCode(TransactionCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }
}