package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.dto.TransferResponse;
import com.rs.payments.wallet.service.WalletService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransferController transferController;

    @Test
    @DisplayName("Should transfer successfully")
    void shouldTransfer() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(fromId);
        request.setToWalletId(toId);
        request.setAmount(new BigDecimal("5.00"));

        TransferResponse transferResponse = new TransferResponse(
                fromId, toId, new BigDecimal("5.00"), new BigDecimal("10.00"), new BigDecimal("15.00"));
        when(walletService.transfer(fromId, toId, new BigDecimal("5.00"))).thenReturn(transferResponse);

        ResponseEntity<TransferResponse> response = transferController.transfer(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(transferResponse, response.getBody());
    }
}
