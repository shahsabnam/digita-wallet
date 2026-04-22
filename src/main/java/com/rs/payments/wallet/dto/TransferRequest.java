package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
@Schema(description = "Transfer request payload")
public class TransferRequest {
    @NotNull
    @Schema(description = "Source wallet ID")
    private UUID fromWalletId;

    @NotNull
    @Schema(description = "Destination wallet ID")
    private UUID toWalletId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Transfer amount", example = "25.50")
    private BigDecimal amount;
}
