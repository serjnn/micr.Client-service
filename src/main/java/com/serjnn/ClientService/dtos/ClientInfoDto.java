package com.serjnn.ClientService.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;


@AllArgsConstructor
@Getter
public class ClientInfoDto {
    private  Long id;
    private  String mail;
    private  BigDecimal balance;
    private  String address;
}