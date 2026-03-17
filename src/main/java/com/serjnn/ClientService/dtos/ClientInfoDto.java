package com.serjnn.ClientService.dtos;

import java.math.BigDecimal;

public record ClientInfoDto(Long id, String mail, BigDecimal balance, String address) {}
