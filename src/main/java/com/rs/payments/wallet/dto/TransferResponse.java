package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Transfer result details")
public class TransferResponse {
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private BigDecimal fromWalletBalance;
    private BigDecimal toWalletBalance;
}
