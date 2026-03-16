package com.serjnn.ClientService.repo;

import com.serjnn.ClientService.models.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClientRepository {
    private final JdbcTemplate jdbcTemplate;

    public Optional<Client> findByMail(String mail) {
        String sql = "SELECT * FROM client WHERE mail = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Client.class), mail)
                .stream()
                .findFirst();
    }

    public Optional<Client> findById(Long id) {
        String sql = "SELECT * FROM client WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Client.class), id)
                .stream()
                .findFirst();
    }

    public void save(Client client) {
        if (client.id() == null) {
            String sql = "INSERT INTO client (mail, password, role, address, balance) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, client.mail(), client.password(), client.role(), client.address(), client.balance());
        } else {
            String sql = "UPDATE client SET mail = ?, password = ?, role = ?, address = ?, balance = ? WHERE id = ?";
            jdbcTemplate.update(sql, client.mail(), client.password(), client.role(), client.address(), client.balance(), client.id());
        }
    }
}
