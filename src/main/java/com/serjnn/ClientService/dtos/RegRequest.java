package com.serjnn.ClientService.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegRequest {
    @Email
    private String mail;
    @Size(min = 7, max = 45)
    private String password;
    @Size(min = 7, max = 45)
    private String repeatPassword;

}
