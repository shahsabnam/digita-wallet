package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.dto.AmountRequest;
import com.rs.payments.wallet.dto.BalanceResponse;
import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
@Tag(name = "Wallet Management", description = "APIs for managing user wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Create a new wallet for a user",
            description = "Creates a new wallet for the specified user ID with a zero balance.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Wallet created successfully",
                            content = @Content(schema = @Schema(implementation = Wallet.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWalletForUser(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Wallet> deposit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.deposit(id, request.getAmount()));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Wallet> withdraw(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.withdraw(id, request.getAmount()));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(new BalanceResponse(id, walletService.getBalance(id)));
    }
}