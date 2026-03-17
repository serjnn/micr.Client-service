package com.serjnn.ClientService.controller;


import com.serjnn.ClientService.dtos.*;
import com.serjnn.ClientService.services.ClientDetailService;
import com.serjnn.ClientService.services.ClientService;
import com.serjnn.ClientService.services.JwtService;
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
public class ClientController {
    private final ClientService clientService;
    private final AuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";


    @PostMapping("/register")
    public ResponseEntity<?> reg(@RequestBody RegRequest regRequest) {
        if (regRequest.mail() == null || regRequest.password() == null) {
            return new ResponseEntity<>("Некоторые обязательные поля отсутствуют", HttpStatus.BAD_REQUEST);
        }
        if (!regRequest.mail().matches(EMAIL_REGEX)) {
            return new ResponseEntity<>("Mail does not math the regex", HttpStatus.BAD_REQUEST);
        }
        clientService.register(regRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/secured")
    public void som() {
    }


    @PostMapping("/auth")
    public ResponseEntity<String> auth(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.mail(),
                    authRequest.password()));
            UserDetails userDetails = clientDetailService.loadUserByUsername(authRequest.mail());
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }


    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            String extractedToken = token.substring(7);
            String username = jwtService.extractUsername(extractedToken);
            UserDetails userDetails = clientDetailService.loadUserByUsername(username);

            if (jwtService.isTokenValid(extractedToken, userDetails)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed");
        }
    }


    @GetMapping("/myInfo")
    public ClientInfoDto clientInfo() {
        return clientService.getClientInfo();
    }

    @GetMapping("/addBalance/{clientId}/{amount}")
    public void addBalance(@PathVariable Long clientId, @PathVariable BigDecimal amount) {
        clientService.addBalance(clientId, amount);
    }

    @PostMapping("/changeAddress")
    public void changeAddress(@RequestParam String address) {
        clientService.setAddress(address);
    }

    @PostMapping("/restore")
    public void restore(@RequestBody OrderDTO orderDTO) {
        clientService.addBalance(orderDTO.clientID(), orderDTO.totalSum());
    }

    @PostMapping("/deduct")
    public void deduct(@RequestBody OrderDTO orderDTO) {
        clientService.deductMoney(orderDTO.clientID(), orderDTO.totalSum());
    }
}
