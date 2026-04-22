package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Wallet balance response")
public class BalanceResponse {
    private UUID walletId;
    private BigDecimal balance;
}
