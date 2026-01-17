package com.example.s32706Bank.repo;

import org.springframework.stereotype.Repository;
import com.example.s32706Bank.model.Client;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

@Repository
public class InMemoryClientRepository implements ClientRepository {

    private final ConcurrentMap<UUID, Client> storage = new ConcurrentHashMap<>();

    @Override
    public Client save(Client client) {
        storage.put(client.getId(), client);
        return client;
    }

    @Override
    public Optional<Client> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return storage.containsKey(id);
    }

    @Override
    public Optional<Client> updateBalance(UUID id, UnaryOperator<BigDecimal> updateFn) {
        Client updated = storage.computeIfPresent(id, (key, existing) -> {
            BigDecimal newBalance = updateFn.apply(existing.getBalance());
            existing.setBalance(newBalance);
            return existing;
        });
        return Optional.ofNullable(updated);
    }
}