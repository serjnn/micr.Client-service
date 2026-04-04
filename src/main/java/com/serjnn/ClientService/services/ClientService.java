package com.serjnn.ClientService.services;

import com.serjnn.ClientService.dtos.ClientInfoDto;
import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ClientService {
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Client findById(Long id) {
        log.debug("Finding client by id: {}", id);
        return clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Client not found with id: {}", id);
                    return new UsernameNotFoundException("User not found with id: " + id);
                });
    }

    public void save(Client client) {
        log.debug("Saving client: {}", client.mail());
        clientRepository.save(client);
    }

    public void register(RegRequest regRequest) {
        log.info("Registering new client with email: {}", regRequest.mail());
        Client client = new Client(regRequest.mail(),
                passwordEncoder.encode(regRequest.password())
        );
        save(client);
        log.info("Client registered: {}", regRequest.mail());
    }

    public Client findByMail(String mail) {
        log.debug("Finding client by mail: {}", mail);
        return clientRepository.findByMail(mail)
                .orElseThrow(() -> {
                    log.error("Client not found with mail: {}", mail);
                    return new UsernameNotFoundException("User not found with mail: " + mail);
                });
    }

    public Client findCurrentClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mail = authentication.getName();
        log.debug("Finding current client for mail: {}", mail);
        return findByMail(mail);
    }

    public void addBalance(Long clientID, BigDecimal balance) {
        log.info("Adding balance {} to client id: {}", balance, clientID);
        clientRepository.updateBalance(clientID, balance);
    }

    public void setAddress(String address) {
        Client client = findCurrentClient();
        log.info("Setting address for client {}: {}", client.mail(), address);
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
        log.info("Deducting {} from client id: {}", amount, clientID);
        try {
            int updated = clientRepository.updateBalance(clientID, amount.negate());
            if (updated == 0) {
                log.error("Failed to deduct money: Client not found id: {}", clientID);
                throw new RuntimeException("Client not found");
            }
            log.info("Successfully deducted {} from client id: {}", amount, clientID);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Failed to deduct money: Insufficient funds for client id: {}", clientID);
            throw new RuntimeException("Insufficient funds");
        }
    }

    public ClientInfoDto getClientInfo() {
        Client client = findCurrentClient();
        log.debug("Returning client info for: {}", client.mail());
        return new ClientInfoDto(client.id()
                , client.mail()
                , client.balance()
                , client.address());
    }
}
