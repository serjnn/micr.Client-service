package com.serjnn.ClientService.services;

import com.serjnn.ClientService.repo.ClientRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClientDetailService implements UserDetailsService {

    private final ClientRepository clientRepository;

    public ClientDetailService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        return clientRepository.findByMail(mail)
                .map(client -> User.builder()
                        .username(client.mail())
                        .password(client.password())
                        .roles(client.role())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with mail: " + mail));
    }
}
