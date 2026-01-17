package com.example.s32706Bank.service;

import org.springframework.stereotype.Service;
import com.example.s32706Bank.dto.TransactionResponse;
import com.example.s32706Bank.model.Client;
import com.example.s32706Bank.model.TransactionCode;
import com.example.s32706Bank.model.TransactionStatus;
import com.example.s32706Bank.repo.ClientRepository;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankService {

    private final ClientRepository clientRepository;

    public BankService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client registerClient(BigDecimal initialBalance) {
        BigDecimal balance = normalizeMoney(Objects.requireNonNull(initialBalance, "initialBalance"));
        UUID id = UUID.randomUUID();
        Client client = new Client(id, balance);
        return clientRepository.save(client);
    }

    public Optional<Client> getClient(UUID clientId) {
        return clientRepository.findById(clientId);
    }

    public TransactionResponse deposit(UUID clientId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return decline(TransactionCode.INVALID_AMOUNT, "Amount must be > 0", null);
        }
        BigDecimal delta = normalizeMoney(amount);

        return clientRepository.updateBalance(clientId, old -> normalizeMoney(old.add(delta)))
                .map(c -> accept("Deposit accepted", c.getBalance()))
                .orElseGet(() -> decline(TransactionCode.CLIENT_NOT_FOUND, "Client not registered", null));
    }

    public TransactionResponse transfer(UUID clientId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return decline(TransactionCode.INVALID_AMOUNT, "Amount must be > 0", null);
        }
        BigDecimal delta = normalizeMoney(amount);

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            return decline(TransactionCode.CLIENT_NOT_FOUND, "Client not registered", null);
        }

        final TransactionResponse[] resultHolder = new TransactionResponse[1];
        clientRepository.updateBalance(clientId, old -> {
            BigDecimal oldNorm = normalizeMoney(old);
            if (oldNorm.compareTo(delta) < 0) {
                resultHolder[0] = decline(TransactionCode.INSUFFICIENT_FUNDS, "Insufficient funds", oldNorm);
                return oldNorm;
            }
            BigDecimal newBal = normalizeMoney(oldNorm.subtract(delta));
            resultHolder[0] = accept("Transfer accepted", newBal);
            return newBal;
        });

        return resultHolder[0] != null
                ? resultHolder[0]
                : decline(TransactionCode.CLIENT_NOT_FOUND, "Client not registered", null);
    }

    private static TransactionResponse accept(String message, BigDecimal newBalance) {
        return new TransactionResponse(TransactionStatus.ACCEPTED, TransactionCode.OK, message, newBalance);
    }

    private static TransactionResponse decline(TransactionCode code, String message, BigDecimal newBalance) {
        return new TransactionResponse(TransactionStatus.DECLINED, code, message, newBalance);
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
