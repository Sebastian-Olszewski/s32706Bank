package com.example.s32706Bank.repo;

import com.example.s32706Bank.model.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    Client save(Client client);

    Optional<Client> findById(UUID id);

    boolean existsById(UUID id);

    Optional<Client> updateBalance(UUID id, java.util.function.UnaryOperator<java.math.BigDecimal> updateFn);
}
