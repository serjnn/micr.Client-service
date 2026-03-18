package com.serjnn.ClientService.repo;

import com.serjnn.ClientService.models.Client;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ClientRepository {
    private final JdbcTemplate jdbcTemplate;

    public ClientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Client> rowMapper = (rs, rowNum) -> new Client(
            rs.getLong("id"),
            rs.getString("mail"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("address"),
            rs.getBigDecimal("balance")
    );

    public Optional<Client> findByMail(String mail) {
        String sql = "SELECT * FROM client WHERE mail = ?";
        return jdbcTemplate.query(sql, rowMapper, mail).stream().findFirst();
    }

    public Optional<Client> findById(Long id) {
        String sql = "SELECT * FROM client WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
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

    public int updateBalance(Long id, java.math.BigDecimal amount) {
        String sql = "UPDATE client SET balance = balance + ? WHERE id = ?";
        return jdbcTemplate.update(sql, amount, id);
    }
}
