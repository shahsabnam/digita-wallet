package com.rs.payments.wallet.service;

import java.util.UUID;
import java.math.BigDecimal;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.dto.TransferResponse;

public interface WalletService {
    Wallet createWalletForUser(UUID userId);
    Wallet deposit(UUID walletId, BigDecimal amount);
    Wallet withdraw(UUID walletId, BigDecimal amount);
    TransferResponse transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount);
    BigDecimal getBalance(UUID walletId);
}