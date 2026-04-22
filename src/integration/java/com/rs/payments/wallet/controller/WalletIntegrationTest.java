package com.rs.payments.wallet.controller;

import java.util.UUID;
import com.rs.payments.wallet.BaseIntegrationTest;
import com.rs.payments.wallet.dto.AmountRequest;
import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import java.math.BigDecimal;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Tag;
import com.rs.payments.wallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class WalletIntegrationTest extends BaseIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateWalletForExistingUser() {
        User user = new User();
        user.setUsername("walletuser");
        user.setEmail("wallet@example.com");
        user = userRepository.save(user);

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(user.getId());

        String url = "http://localhost:" + port + "/wallets";
        ResponseEntity<Wallet> response = restTemplate.postForEntity(url, request, Wallet.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(UUID.randomUUID());

        String url = "http://localhost:" + port + "/wallets";
        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void shouldReturnBadRequestWhenUserAlreadyHasWallet() {
        User user = new User();
        user.setUsername("walletuser2");
        user.setEmail("wallet2@example.com");
        user = userRepository.save(user);

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(user.getId());
        String url = "http://localhost:" + port + "/wallets";
        ResponseEntity<Wallet> first = restTemplate.postForEntity(url, request, Wallet.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void shouldDepositWithdrawAndGetBalance() {
        Wallet wallet = createWalletForUser("walletuser3", "wallet3@example.com");
        String baseUrl = "http://localhost:" + port + "/wallets/" + wallet.getId();

        AmountRequest depositRequest = new AmountRequest();
        depositRequest.setAmount(new BigDecimal("100.00"));
        ResponseEntity<Wallet> depositResponse = restTemplate.postForEntity(
                baseUrl + "/deposit", depositRequest, Wallet.class);
        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(depositResponse.getBody()).isNotNull();
        assertThat(depositResponse.getBody().getBalance()).isEqualByComparingTo("100.00");

        AmountRequest withdrawRequest = new AmountRequest();
        withdrawRequest.setAmount(new BigDecimal("40.00"));
        ResponseEntity<Wallet> withdrawResponse = restTemplate.postForEntity(
                baseUrl + "/withdraw", withdrawRequest, Wallet.class);
        assertThat(withdrawResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withdrawResponse.getBody()).isNotNull();
        assertThat(withdrawResponse.getBody().getBalance()).isEqualByComparingTo("60.00");

        ResponseEntity<JsonNode> balanceResponse = restTemplate.getForEntity(baseUrl + "/balance", JsonNode.class);
        assertThat(balanceResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(balanceResponse.getBody()).isNotNull();
        assertThat(balanceResponse.getBody().get("walletId").asText()).isEqualTo(wallet.getId().toString());
        assertThat(balanceResponse.getBody().get("balance").decimalValue())
                .isEqualByComparingTo("60.00");
    }

    @Test
    void shouldFailWithdrawWithInsufficientFunds() {
        Wallet wallet = createWalletForUser("walletuser4", "wallet4@example.com");
        String baseUrl = "http://localhost:" + port + "/wallets/" + wallet.getId();

        AmountRequest withdrawRequest = new AmountRequest();
        withdrawRequest.setAmount(new BigDecimal("10.00"));
        try {
            restTemplate.postForEntity(baseUrl + "/withdraw", withdrawRequest, String.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<JsonNode> balanceResponse = restTemplate.getForEntity(baseUrl + "/balance", JsonNode.class);
        assertThat(balanceResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(balanceResponse.getBody().get("balance").decimalValue())
                .isEqualByComparingTo("0.00");
    }

    @Test
    void shouldTransferBetweenWalletsAndFailWhenInsufficientFunds() {
        Wallet fromWallet = createWalletForUser("walletuser5", "wallet5@example.com");
        Wallet toWallet = createWalletForUser("walletuser6", "wallet6@example.com");
        String fromBaseUrl = "http://localhost:" + port + "/wallets/" + fromWallet.getId();
        String toBaseUrl = "http://localhost:" + port + "/wallets/" + toWallet.getId();

        AmountRequest depositRequest = new AmountRequest();
        depositRequest.setAmount(new BigDecimal("70.00"));
        restTemplate.postForEntity(fromBaseUrl + "/deposit", depositRequest, Wallet.class);

        String transferUrl = "http://localhost:" + port + "/transfers";
        Map<String, Object> transferRequest = Map.of(
                "fromWalletId", fromWallet.getId(),
                "toWalletId", toWallet.getId(),
                "amount", "20.00");
        ResponseEntity<JsonNode> transferResponse = restTemplate.postForEntity(transferUrl, transferRequest, JsonNode.class);
        assertThat(transferResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<JsonNode> fromBalanceAfterSuccess = restTemplate.getForEntity(fromBaseUrl + "/balance", JsonNode.class);
        ResponseEntity<JsonNode> toBalanceAfterSuccess = restTemplate.getForEntity(toBaseUrl + "/balance", JsonNode.class);
        assertThat(fromBalanceAfterSuccess.getBody().get("balance").decimalValue())
                .isEqualByComparingTo("50.00");
        assertThat(toBalanceAfterSuccess.getBody().get("balance").decimalValue())
                .isEqualByComparingTo("20.00");

        Map<String, Object> failingTransferRequest = Map.of(
                "fromWalletId", fromWallet.getId(),
                "toWalletId", toWallet.getId(),
                "amount", "200.00");
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(failingTransferRequest);
        try {
            restTemplate.exchange(transferUrl, HttpMethod.POST, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<JsonNode> fromBalanceAfterFail = restTemplate.getForEntity(fromBaseUrl + "/balance", JsonNode.class);
        ResponseEntity<JsonNode> toBalanceAfterFail = restTemplate.getForEntity(toBaseUrl + "/balance", JsonNode.class);
        assertThat(fromBalanceAfterFail.getBody().get("balance").decimalValue())
                .isEqualByComparingTo("50.00");
        assertThat(toBalanceAfterFail.getBody().get("balance").decimalValue())
                .isEqualByComparingTo("20.00");
    }

    private Wallet createWalletForUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user = userRepository.save(user);

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(user.getId());

        String url = "http://localhost:" + port + "/wallets";
        ResponseEntity<Wallet> response = restTemplate.postForEntity(url, request, Wallet.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
