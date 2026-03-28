package com.serjnn.ClientService.services;

import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.RegRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ClientService clientService;
    private final AuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public AuthService(ClientService clientService,
                       AuthenticationManager authenticationManager,
                       ClientDetailService clientDetailService,
                       JwtService jwtService) {
        this.clientService = clientService;
        this.authenticationManager = authenticationManager;
        this.clientDetailService = clientDetailService;
        this.jwtService = jwtService;
    }

    public void register(RegRequest regRequest) {
        if (regRequest.mail() == null || regRequest.password() == null) {
            throw new IllegalArgumentException("Required fields missing");
        }
        if (!regRequest.mail().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        clientService.register(regRequest);
    }

    public String login(AuthRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.mail(), authRequest.password()));
        UserDetails userDetails = clientDetailService.loadUserByUsername(authRequest.mail());
        return jwtService.generateToken(userDetails);
    }

    public boolean validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        String extractedToken = token.substring(7);
        String username = jwtService.extractUsername(extractedToken);
        UserDetails userDetails = clientDetailService.loadUserByUsername(username);

        return jwtService.isTokenValid(extractedToken, userDetails);
    }
}
