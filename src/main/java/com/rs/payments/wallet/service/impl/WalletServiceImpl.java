package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.dto.TransferResponse;
import com.rs.payments.wallet.exception.BadRequestException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.Transaction;
import com.rs.payments.wallet.model.TransactionType;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import com.rs.payments.wallet.service.WalletService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public Wallet createWalletForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getWallet() != null) {
            throw new BadRequestException("User already has a wallet");
        }

        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);
        user.setWallet(wallet);

        user = userRepository.save(user); // Cascade saves wallet
        return user.getWallet();
    }

    @Override
    @Transactional
    public Wallet deposit(UUID walletId, BigDecimal amount) {
        validateAmount(amount);
        Wallet wallet = getWalletForUpdate(walletId);
        wallet.setBalance(wallet.getBalance().add(amount));
        createTransaction(wallet, amount, TransactionType.DEPOSIT, "Wallet deposit");
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet withdraw(UUID walletId, BigDecimal amount) {
        validateAmount(amount);
        Wallet wallet = getWalletForUpdate(walletId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        createTransaction(wallet, amount, TransactionType.WITHDRAWAL, "Wallet withdrawal");
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public TransferResponse transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        validateAmount(amount);
        if (fromWalletId.equals(toWalletId)) {
            throw new BadRequestException("Cannot transfer to the same wallet");
        }

        Wallet fromWallet = getWalletForUpdate(fromWalletId);
        Wallet toWallet = getWalletForUpdate(toWalletId);
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        createTransaction(fromWallet, amount, TransactionType.TRANSFER_OUT, "Transfer to wallet " + toWalletId);
        createTransaction(toWallet, amount, TransactionType.TRANSFER_IN, "Transfer from wallet " + fromWalletId);

        return new TransferResponse(
                fromWalletId,
                toWalletId,
                amount,
                fromWallet.getBalance(),
                toWallet.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"))
                .getBalance();
    }

    private Wallet getWalletForUpdate(UUID walletId) {
        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

    private void createTransaction(Wallet wallet, BigDecimal amount, TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
}