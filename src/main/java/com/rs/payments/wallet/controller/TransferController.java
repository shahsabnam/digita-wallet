package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.dto.TransferResponse;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
@Tag(name = "Transfer Management", description = "APIs for peer-to-peer transfers")
public class TransferController {
    private final WalletService walletService;

    public TransferController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Perform a new transfer",
            description = "Transfers an amount from one wallet to another",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer completed successfully",
                            content = @Content(schema = @Schema(implementation = TransferResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Wallet not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid transfer request"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(walletService.transfer(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount()));
    }
}
