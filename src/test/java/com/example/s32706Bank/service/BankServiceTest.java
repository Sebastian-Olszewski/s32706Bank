package com.example.s32706Bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.s32706Bank.dto.TransactionResponse;
import com.example.s32706Bank.model.Client;
import com.example.s32706Bank.model.TransactionCode;
import com.example.s32706Bank.model.TransactionStatus;
import com.example.s32706Bank.repo.ClientRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private ClientRepository clientRepository;

    private BankService bankService;

    @BeforeEach
    void setUp() {
        bankService = new BankService(clientRepository);
    }


    @Test
    void registerClient_savesClientAndNormalizesScale() {
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client client = bankService.registerClient(new BigDecimal("10"));

        assertNotNull(client.getId());
        assertEquals(new BigDecimal("10.00"), client.getBalance());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertEquals(client.getId(), captor.getValue().getId());
        assertEquals(new BigDecimal("10.00"), captor.getValue().getBalance());
    }

    @Test
    void registerClient_roundsHalfUpTo2dp() {
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client client = bankService.registerClient(new BigDecimal("10.005"));

        // 10.005 -> 10.01 (HALF_UP)
        assertEquals(new BigDecimal("10.01"), client.getBalance());
        verify(clientRepository).save(any(Client.class));
    }


    @Test
    void getClient_returnsOptionalFromRepository() {
        UUID id = UUID.randomUUID();
        Client client = new Client(id, new BigDecimal("1.00"));

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));

        Optional<Client> res = bankService.getClient(id);

        assertTrue(res.isPresent());
        assertEquals(id, res.get().getId());
        verify(clientRepository).findById(id);
    }

    @Test
    void getClient_returnsEmptyWhenMissing() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Client> res = bankService.getClient(id);

        assertTrue(res.isEmpty());
        verify(clientRepository).findById(id);
    }


    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void deposit_declinesWhenAmountNotPositive(String amountStr) {
        UUID id = UUID.randomUUID();

        TransactionResponse res = bankService.deposit(id, new BigDecimal(amountStr));

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.INVALID_AMOUNT, res.getCode());
        assertNull(res.getNewBalance());
        verifyNoInteractions(clientRepository);
    }

    @Test
    void deposit_declinesWhenAmountNull() {
        UUID id = UUID.randomUUID();

        TransactionResponse res = bankService.deposit(id, null);

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.INVALID_AMOUNT, res.getCode());
        verifyNoInteractions(clientRepository);
    }

    @Test
    void deposit_acceptsAndIncreasesBalance_normalizesTo2dp() {
        UUID id = UUID.randomUUID();
        Client client = new Client(id, new BigDecimal("5.00"));

        when(clientRepository.updateBalance(eq(id), any())).thenAnswer(inv -> {
            UnaryOperator<BigDecimal> fn = inv.getArgument(1);
            client.setBalance(fn.apply(client.getBalance()));
            return Optional.of(client);
        });

        TransactionResponse res = bankService.deposit(id, new BigDecimal("2"));

        assertEquals(TransactionStatus.ACCEPTED, res.getStatus());
        assertEquals(TransactionCode.OK, res.getCode());
        assertEquals(new BigDecimal("7.00"), res.getNewBalance());
        assertEquals(new BigDecimal("7.00"), client.getBalance());
        verify(clientRepository).updateBalance(eq(id), any());
    }

    @Test
    void deposit_acceptsAndRoundsAmount_halfUp() {
        UUID id = UUID.randomUUID();
        Client client = new Client(id, new BigDecimal("0.00"));

        when(clientRepository.updateBalance(eq(id), any())).thenAnswer(inv -> {
            UnaryOperator<BigDecimal> fn = inv.getArgument(1);
            client.setBalance(fn.apply(client.getBalance()));
            return Optional.of(client);
        });

        TransactionResponse res = bankService.deposit(id, new BigDecimal("1.005"));

        assertEquals(TransactionStatus.ACCEPTED, res.getStatus());
        assertEquals(TransactionCode.OK, res.getCode());
        // 1.005 -> 1.01
        assertEquals(new BigDecimal("1.01"), res.getNewBalance());
        assertEquals(new BigDecimal("1.01"), client.getBalance());
    }

    @Test
    void deposit_declinesWhenClientNotFound() {
        UUID id = UUID.randomUUID();
        when(clientRepository.updateBalance(eq(id), any())).thenReturn(Optional.empty());

        TransactionResponse res = bankService.deposit(id, new BigDecimal("1"));

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.CLIENT_NOT_FOUND, res.getCode());
        assertNull(res.getNewBalance());
        verify(clientRepository).updateBalance(eq(id), any());
    }


    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void transfer_declinesWhenAmountNotPositive(String amountStr) {
        UUID id = UUID.randomUUID();

        TransactionResponse res = bankService.transfer(id, new BigDecimal(amountStr));

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.INVALID_AMOUNT, res.getCode());
        assertNull(res.getNewBalance());
        verifyNoInteractions(clientRepository);
    }

    @Test
    void transfer_declinesWhenAmountNull() {
        UUID id = UUID.randomUUID();

        TransactionResponse res = bankService.transfer(id, null);

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.INVALID_AMOUNT, res.getCode());
        verifyNoInteractions(clientRepository);
    }

    @Test
    void transfer_declinesWhenClientNotFound_andDoesNotUpdateBalance() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        TransactionResponse res = bankService.transfer(id, new BigDecimal("1"));

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.CLIENT_NOT_FOUND, res.getCode());
        verify(clientRepository).findById(id);
        verify(clientRepository, never()).updateBalance(any(), any());
    }

    @Test
    void transfer_acceptsAndDecreasesBalance() {
        UUID id = UUID.randomUUID();
        Client client = new Client(id, new BigDecimal("10.00"));

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientRepository.updateBalance(eq(id), any())).thenAnswer(inv -> {
            UnaryOperator<BigDecimal> fn = inv.getArgument(1);
            client.setBalance(fn.apply(client.getBalance()));
            return Optional.of(client);
        });

        TransactionResponse res = bankService.transfer(id, new BigDecimal("3"));

        assertEquals(TransactionStatus.ACCEPTED, res.getStatus());
        assertEquals(TransactionCode.OK, res.getCode());
        assertEquals(new BigDecimal("7.00"), res.getNewBalance());
        assertEquals(new BigDecimal("7.00"), client.getBalance());
        verify(clientRepository).findById(id);
        verify(clientRepository).updateBalance(eq(id), any());
    }

    @Test
    void transfer_declinesWhenInsufficientFunds_balanceUnchanged() {
        UUID id = UUID.randomUUID();
        Client client = new Client(id, new BigDecimal("2.00"));

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientRepository.updateBalance(eq(id), any())).thenAnswer(inv -> {
            UnaryOperator<BigDecimal> fn = inv.getArgument(1);
            client.setBalance(fn.apply(client.getBalance()));
            return Optional.of(client);
        });

        TransactionResponse res = bankService.transfer(id, new BigDecimal("3"));

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.INSUFFICIENT_FUNDS, res.getCode());
        assertEquals(new BigDecimal("2.00"), res.getNewBalance());
        assertEquals(new BigDecimal("2.00"), client.getBalance());
        verify(clientRepository).findById(id);
        verify(clientRepository).updateBalance(eq(id), any());
    }

    @Test
    void transfer_insufficientFunds_isDeterministic_evenWithRounding() {
        UUID id = UUID.randomUUID();
        Client client = new Client(id, new BigDecimal("1.00"));

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientRepository.updateBalance(eq(id), any())).thenAnswer(inv -> {
            UnaryOperator<BigDecimal> fn = inv.getArgument(1);
            client.setBalance(fn.apply(client.getBalance()));
            return Optional.of(client);
        });

        TransactionResponse res = bankService.transfer(id, new BigDecimal("1.005"));

        assertEquals(TransactionStatus.DECLINED, res.getStatus());
        assertEquals(TransactionCode.INSUFFICIENT_FUNDS, res.getCode());
        assertEquals(new BigDecimal("1.00"), res.getNewBalance());
        assertEquals(new BigDecimal("1.00"), client.getBalance());
    }
}