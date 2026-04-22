package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "Amount payload")
public class AmountRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Transaction amount", example = "100.00")
    private BigDecimal amount;
}
