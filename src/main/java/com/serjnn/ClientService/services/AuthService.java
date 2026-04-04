package com.serjnn.ClientService.services;

import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.RegRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
        log.debug("AuthService: registering {}", regRequest.mail());
        if (regRequest.mail() == null || regRequest.password() == null) {
            log.error("Registration failed: Required fields missing");
            throw new IllegalArgumentException("Required fields missing");
        }
        if (!regRequest.mail().matches(EMAIL_REGEX)) {
            log.error("Registration failed: Invalid email format {}", regRequest.mail());
            throw new IllegalArgumentException("Invalid email format");
        }
        clientService.register(regRequest);
    }

    public String login(AuthRequest authRequest) {
        log.debug("AuthService: authenticating {}", authRequest.mail());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.mail(), authRequest.password()));
        UserDetails userDetails = clientDetailService.loadUserByUsername(authRequest.mail());
        String token = jwtService.generateToken(userDetails);
        log.debug("AuthService: token generated for {}", authRequest.mail());
        return token;
    }

    public boolean validateToken(String token) {
        log.debug("AuthService: validating token");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("AuthService: Token is null or does not start with Bearer");
            return false;
        }
        String extractedToken = token.substring(7);
        String username = jwtService.extractUsername(extractedToken);
        log.debug("AuthService: extracted username {} from token", username);
        UserDetails userDetails = clientDetailService.loadUserByUsername(username);

        boolean isValid = jwtService.isTokenValid(extractedToken, userDetails);
        log.debug("AuthService: token valid: {}", isValid);
        return isValid;
    }
}
