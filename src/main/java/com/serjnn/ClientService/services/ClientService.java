package com.serjnn.ClientService.services;

import com.serjnn.ClientService.dtos.ClientInfoDto;
import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public void save(Client client) {
        clientRepository.save(client);
    }

    public void register(RegRequest regRequest) {
        Client client = new Client(regRequest.mail(),
                passwordEncoder.encode(regRequest.password())
        );
        save(client);
    }

    public Client findByMail(String mail) {
        return clientRepository.findByMail(mail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with mail: " + mail));
    }

    public Client findCurrentClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return findByMail(authentication.getName());
    }

    public void addBalance(Long clientID, BigDecimal balance) {
        clientRepository.updateBalance(clientID, balance);
    }

    public void setAddress(String address) {
        Client client = findCurrentClient();
        Client updatedClient = new Client(
                client.id(),
                client.mail(),
                client.password(),
                client.role(),
                address,
                client.balance()
        );
        save(updatedClient);
    }

    @Transactional
    public void deductMoney(Long clientID, BigDecimal amount) {
        try {
            int updated = clientRepository.updateBalance(clientID, amount.negate());
            if (updated == 0) {
                throw new RuntimeException("Client not found");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Insufficient funds");
        }
    }

    public ClientInfoDto getClientInfo() {
        Client client = findCurrentClient();
        return new ClientInfoDto(client.id()
                , client.mail()
                , client.balance()
                , client.address());
    }
}
