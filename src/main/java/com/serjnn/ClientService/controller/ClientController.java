package com.serjnn.ClientService.controller;


import com.serjnn.ClientService.dtos.AuthRequest;
import com.serjnn.ClientService.dtos.ClientInfoDto;
import com.serjnn.ClientService.dtos.OrderDTO;
import com.serjnn.ClientService.dtos.RegRequest;
import com.serjnn.ClientService.models.Client;
import com.serjnn.ClientService.repo.ClientRepository;
import com.serjnn.ClientService.services.ClientService;
import com.serjnn.ClientService.services.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClientController {
    private final ClientService clientService;

    private final JwtService jwtService;

    private final ClientRepository clientRepository;


    @GetMapping("/id/{id}")
    Mono<Client > client (@PathVariable("id") long id ){
        return clientRepository.findById(id);
    }

    @PostMapping("/register")
    Mono<Void> reg(@RequestBody @Valid RegRequest regRequest) {
         return clientService.register(regRequest);
    }

    @PostMapping("/auth")
    Mono<ResponseEntity<String>> auth(@RequestBody AuthRequest authRequest) {
        return clientService.auth(authRequest);
    }
    @GetMapping("/secured")
    Mono<Object> secured() {
        return Mono.empty();
    }

    @GetMapping("/myInfo")
    Mono<ClientInfoDto> clientInfo() {
        return clientService.getClientInfo();
    }

    @GetMapping("/addBalance/{clientId}/{amount}")
    Mono<Void> addBalance(@PathVariable Long clientId, @PathVariable BigDecimal amount) {
        return clientService.addBalance(clientId, amount);
    }

    @PostMapping("/changeAddress")
    Mono<Void> changeAddress(@RequestParam String address) {
        return clientService.setAddress(address);

    }

    @PostMapping("/restore")
    Mono<Void> restore(@RequestBody OrderDTO orderDTO) {
        return clientService.addBalance(orderDTO.getClientId(), orderDTO.getTotalSum());

    }

    @PostMapping("/deduct")
    Mono<Void> deduct(@RequestBody OrderDTO orderDTO) {
        System.out.println(orderDTO);
        return clientService.deductMoney(orderDTO.getClientId(), orderDTO.getTotalSum());

    }


}
