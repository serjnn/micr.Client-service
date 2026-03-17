package com.serjnn.ClientService.controller;

import com.serjnn.ClientService.dtos.*;
import com.serjnn.ClientService.services.ClientDetailService;
import com.serjnn.ClientService.services.ClientService;
import com.serjnn.ClientService.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Client API", description = "Operations related to clients")
public class ClientController {

    private final ClientService clientService;
    private final AuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    @PostMapping("/clients/register")
    @Operation(summary = "Register a new client")
    public ResponseEntity<?> register(@RequestBody RegRequest regRequest) {
        if (regRequest.mail() == null || regRequest.password() == null) {
            return new ResponseEntity<>("Required fields missing", HttpStatus.BAD_REQUEST);
        }
        if (!regRequest.mail().matches(EMAIL_REGEX)) {
            return new ResponseEntity<>("Invalid email format", HttpStatus.BAD_REQUEST);
        }
        clientService.register(regRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate client and return JWT")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.mail(), authRequest.password()));
            UserDetails userDetails = clientDetailService.loadUserByUsername(authRequest.mail());
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/auth/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (!token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
            }
            String extractedToken = token.substring(7);
            String username = jwtService.extractUsername(extractedToken);
            UserDetails userDetails = clientDetailService.loadUserByUsername(username);

            if (jwtService.isTokenValid(extractedToken, userDetails)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed");
        }
    }

    @GetMapping("/clients/me")
    @Operation(summary = "Get current client information")
    public ResponseEntity<ClientInfoDto> getMyInfo() {
        return ResponseEntity.ok(clientService.getClientInfo());
    }

    @PatchMapping("/clients/{clientId}/balance")
    @Operation(summary = "Add balance to a specific client")
    public ResponseEntity<Void> addBalance(@PathVariable Long clientId, @RequestParam BigDecimal amount) {
        clientService.addBalance(clientId, amount);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/clients/me/address")
    @Operation(summary = "Update current client address")
    public ResponseEntity<Void> updateAddress(@RequestParam String address) {
        clientService.setAddress(address);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clients/transactions/restore")
    @Operation(summary = "Restore balance from an order")
    public ResponseEntity<Void> restoreBalance(@RequestBody OrderDTO orderDTO) {
        clientService.addBalance(orderDTO.clientID(), orderDTO.totalSum());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clients/transactions/deduct")
    @Operation(summary = "Deduct balance for an order")
    public ResponseEntity<Void> deductBalance(@RequestBody OrderDTO orderDTO) {
        clientService.deductMoney(orderDTO.clientID(), orderDTO.totalSum());
        return ResponseEntity.ok().build();
    }
}
