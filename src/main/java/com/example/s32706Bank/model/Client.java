package com.example.s32706Bank.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Client {
    private final UUID id;
    private BigDecimal balance;
    private final Instant createdAt;

    public Client(UUID id, BigDecimal balance) {
        this.id = Objects.requireNonNull(id, "id");
        this.balance = Objects.requireNonNull(balance, "balance");
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = Objects.requireNonNull(balance, "balance");
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}