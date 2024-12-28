package com.serjnn.ClientService.services;


import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.ClientInfoDto;
import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ClientDetailService clientDetailService;
    private final JwtService jwtService;


    private Mono<Client> findById(Long id) {
        return clientRepository.findById(id)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with id: " + id)));
    }

    public Mono<Void> save(Client client) {
        return clientRepository.save(client).then();
    }

    public Mono<Void> register(RegRequest regRequest) {
        Client client = new Client(regRequest.getMail(),
                passwordEncoder.encode(regRequest.getPassword())
        );
        return save(client);

    }

    public Mono<Client> findByMail(String mail) {
        return clientRepository.findByMail(mail);

    }

    public Mono<Client> findCurrentClient() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(this::findByMail);
    }

    public Mono<Void> addBalance(Long clientID, BigDecimal balance) {
        return findById(clientID)
                .map(client -> {
                    client.setBalance(client.getBalance().add(balance));
                    return client;
                })
                .flatMap(this::save);

    }


    public Mono<Void> setAddress(String address) {
        return findCurrentClient()
                .map(client -> {
                    client.setAddress(address);
                    return client;
                })
                .flatMap(this::save);

    }

    public Mono<Void> deductMoney(Long clientID, BigDecimal amount) {
        return findById(clientID)
                .flatMap(client -> {
                    if (client.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new RuntimeException("Insufficient funds"));
                    }
                    client.setBalance(client.getBalance().subtract(amount));
                    return save(client);
                });
    }


    public Mono<ClientInfoDto> getClientInfo() {
        return findCurrentClient()
                .map(client ->
                        new ClientInfoDto(client.getId(),
                                client.getMail(),
                                client.getBalance(),
                                client.getAddress()));

    }

    public Mono<ResponseEntity<String>> auth(AuthRequest authRequest) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getMail(),
                        authRequest.getPassword()))
                .flatMap(authentication -> clientDetailService.findByUsername(authRequest.getMail()))
                .flatMap(userDetails -> {
                    if (userDetails == null) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"));
                    }
                    String token = jwtService.generateToken(userDetails);
                    return Mono.just(ResponseEntity.ok(token));
                })
                .onErrorResume(BadCredentialsException.class, e -> Mono.just(ResponseEntity.badRequest().build()));

    }
}
