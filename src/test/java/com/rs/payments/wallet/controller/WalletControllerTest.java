package com.rs.payments.wallet.controller;

import java.util.UUID;
import com.rs.payments.wallet.dto.AmountRequest;
import com.rs.payments.wallet.dto.BalanceResponse;
import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @Test
    @DisplayName("Should create wallet")
    void shouldCreateWallet() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletService.createWalletForUser(userId)).thenReturn(wallet);

        // When
        ResponseEntity<Wallet> response = walletController.createWallet(request);

        // Then
        assertEquals(201, response.getStatusCode().value());
        assertEquals(wallet, response.getBody());
        verify(walletService, times(1)).createWalletForUser(userId);
    }

    @Test
    @DisplayName("Should deposit into wallet")
    void shouldDeposit() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("30.00"));
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal("10.00"));
        when(walletService.deposit(walletId, request.getAmount())).thenReturn(wallet);

        ResponseEntity<Wallet> response = walletController.deposit(walletId, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(wallet, response.getBody());
    }

    @Test
    @DisplayName("Should get wallet balance")
    void shouldGetBalance() {
        UUID walletId = UUID.randomUUID();
        when(walletService.getBalance(walletId)).thenReturn(new BigDecimal("70.00"));

        ResponseEntity<BalanceResponse> response = walletController.getBalance(walletId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(walletId, response.getBody().getWalletId());
        assertEquals(new BigDecimal("70.00"), response.getBody().getBalance());
    }
}
