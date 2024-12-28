package com.serjnn.ClientService.services;

import com.serjnn.ClientService.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ClientDetailService implements ReactiveUserDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public Mono<UserDetails> findByUsername(String mail) {
        return clientRepository.findByMail(mail)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("")))
                .map(client -> User.builder()
                        .username(client.getMail())
                        .password(client.getPassword())
                        .roles(new String[]{client.getRole()})
                        .build());


    }
}