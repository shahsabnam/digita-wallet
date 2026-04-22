package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.dto.TransferResponse;
import com.rs.payments.wallet.exception.BadRequestException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.TransactionType;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    @DisplayName("Should create wallet for existing user")
    void shouldCreateWalletForExistingUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // The service saves the user, which cascades to wallet. 
        // We mock save to return the user.
        when(userRepository.save(user)).thenReturn(user);

        // When
        Wallet result = walletService.createWalletForUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.createWalletForUser(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject creating second wallet for same user")
    void shouldRejectSecondWallet() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setWallet(new Wallet());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> walletService.createWalletForUser(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deposit and create transaction")
    void shouldDeposit() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("10.00"));
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet result = walletService.deposit(walletId, new BigDecimal("5.00"));

        assertEquals(new BigDecimal("15.00"), result.getBalance());
        verify(transactionRepository).save(argThat(t ->
                t.getType() == TransactionType.DEPOSIT && t.getAmount().compareTo(new BigDecimal("5.00")) == 0));
    }

    @Test
    @DisplayName("Should fail withdrawal when insufficient funds")
    void shouldFailWithdrawOnInsufficientFunds() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("4.00"));
        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(BadRequestException.class, () -> walletService.withdraw(walletId, new BigDecimal("5.00")));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should transfer between wallets and create two transactions")
    void shouldTransfer() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromId);
        fromWallet.setBalance(new BigDecimal("20.00"));
        Wallet toWallet = new Wallet();
        toWallet.setId(toId);
        toWallet.setBalance(new BigDecimal("3.00"));
        when(walletRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toWallet));

        TransferResponse response = walletService.transfer(fromId, toId, new BigDecimal("7.00"));

        assertEquals(new BigDecimal("13.00"), response.getFromWalletBalance());
        assertEquals(new BigDecimal("10.00"), response.getToWalletBalance());
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Should get wallet balance")
    void shouldGetBalance() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("99.99"));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertEquals(new BigDecimal("99.99"), walletService.getBalance(walletId));
    }
}
