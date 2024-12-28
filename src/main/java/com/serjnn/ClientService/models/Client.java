package com.serjnn.ClientService.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;


@NoArgsConstructor
@Getter
@Setter
@Table(name = "client")
public class Client {
    @Id
    private Long id;
    private String mail;
    private String password;
    private String role;
    private String address;
    private BigDecimal balance = new BigDecimal(0);

    public Client(String mail, String password) {
        this.mail = mail;
        this.password = password;
        this.role = "client";
    }


}
