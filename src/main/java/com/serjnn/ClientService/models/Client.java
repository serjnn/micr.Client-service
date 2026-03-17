package com.serjnn.ClientService.models;

import java.math.BigDecimal;

public record Client(
    Long id,
    String mail,
    String password,
    String role,
    String address,
    BigDecimal balance
) {
    public Client(String mail, String password) {
        this(null, mail, password, "client", null, BigDecimal.ZERO);
    }
}
