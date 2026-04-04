package com.serjnn.ClientService.controller;

import com.serjnn.ClientService.dtos.*;
import com.serjnn.ClientService.services.AuthService;
import com.serjnn.ClientService.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Client API", description = "Operations related to clients")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;
    private final AuthService authService;

    public ClientController(ClientService clientService, AuthService authService) {
        this.clientService = clientService;
        this.authService = authService;
    }

    @PostMapping("/clients/register")
    @Operation(summary = "Register a new client")
    public ResponseEntity<?> register(@RequestBody RegRequest regRequest) {
        log.info("Received registration request for email: {}", regRequest.mail());
        try {
            authService.register(regRequest);
            log.info("Successfully registered client with email: {}", regRequest.mail());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            log.error("Registration failed for email: {}. Reason: {}", regRequest.mail(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate client and return JWT")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
        log.info("Received login request for email: {}", authRequest.mail());
        try {
            String token = authService.login(authRequest);
            log.info("Successfully authenticated client with email: {}", authRequest.mail());
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            log.error("Login failed for email: {}. Reason: Invalid credentials", authRequest.mail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/auth/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        log.info("Validating token");
        try {
            if (authService.validateToken(token)) {
                log.info("Token validation successful");
                return ResponseEntity.ok().build();
            }
            log.warn("Token validation failed: Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            log.error("Token validation failed with error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed");
        }
    }

    @GetMapping("/clients/me")
    @Operation(summary = "Get current client information")
    public ResponseEntity<ClientInfoDto> getMyInfo() {
        log.info("Fetching info for current client");
        ClientInfoDto info = clientService.getClientInfo();
        log.info("Successfully fetched info for client: {}", info.mail());
        return ResponseEntity.ok(info);
    }

    @PatchMapping("/clients/{clientId}/balance")
    @Operation(summary = "Add balance to a specific client")
    public ResponseEntity<Void> addBalance(@PathVariable Long clientId, @RequestParam BigDecimal amount) {
        log.info("Adding balance {} to client id: {}", amount, clientId);
        clientService.addBalance(clientId, amount);
        log.info("Successfully updated balance for client id: {}", clientId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/clients/me/address")
    @Operation(summary = "Update current client address")
    public ResponseEntity<Void> updateAddress(@RequestParam String address) {
        log.info("Updating address for current client");
        clientService.setAddress(address);
        log.info("Successfully updated address");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clients/transactions/restore")
    @Operation(summary = "Restore balance from an order")
    public ResponseEntity<Void> restoreBalance(@RequestBody OrderDTO orderDTO) {
        log.info("Restoring balance for client id: {} from order: {}", orderDTO.clientID(), orderDTO.orderId());
        clientService.addBalance(orderDTO.clientID(), orderDTO.totalSum());
        log.info("Successfully restored balance for client id: {}", orderDTO.clientID());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clients/transactions/deduct")
    @Operation(summary = "Deduct balance for an order")
    public ResponseEntity<Void> deductBalance(@RequestBody OrderDTO orderDTO) {
        log.info("Deducting balance for client id: {} for order: {}", orderDTO.clientID(), orderDTO.orderId());
        clientService.deductMoney(orderDTO.clientID(), orderDTO.totalSum());
        log.info("Successfully deducted balance for client id: {}", orderDTO.clientID());
        return ResponseEntity.ok().build();
    }
}
