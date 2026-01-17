package com.example.s32706Bank.integration;

import com.example.s32706Bank.dto.*;
import com.example.s32706Bank.model.Client;
import com.example.s32706Bank.service.BankService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    public ResponseEntity<RegisterClientResponse> register(@Valid @RequestBody RegisterClientRequest request) {
        Client client = bankService.registerClient(request.getInitialBalance());
        RegisterClientResponse response = new RegisterClientResponse(client.getId().toString(), client.getBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable UUID clientId) {
        return bankService.getClient(clientId)
                .map(c -> ResponseEntity.ok(new ClientResponse(c.getId().toString(), c.getBalance(), c.getCreatedAt())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{clientId}/deposit")
    public TransactionResponse deposit(@PathVariable UUID clientId, @Valid @RequestBody AmountRequest request) {
        return bankService.deposit(clientId, request.getAmount());
    }

    @PostMapping("/{clientId}/transfer")
    public TransactionResponse transfer(@PathVariable UUID clientId, @Valid @RequestBody AmountRequest request) {
        return bankService.transfer(clientId, request.getAmount());
    }
}