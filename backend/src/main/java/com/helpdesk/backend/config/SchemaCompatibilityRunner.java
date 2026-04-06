package com.helpdesk.backend.config;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SchemaCompatibilityRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        String databaseProduct = jdbcTemplate.queryForObject("select database()", String.class);
        if (databaseProduct == null) {
            return;
        }

        List<String> statements = List.of(
                "ALTER TABLE users MODIFY COLUMN team VARCHAR(32)",
                "ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL",
                "ALTER TABLE users MODIFY COLUMN availability_status VARCHAR(32) NOT NULL",
                "ALTER TABLE users MODIFY COLUMN experience_level VARCHAR(32) NOT NULL",
                "ALTER TABLE tickets MODIFY COLUMN issue_type VARCHAR(32) NOT NULL",
                "ALTER TABLE tickets MODIFY COLUMN priority VARCHAR(32) NOT NULL",
                "ALTER TABLE tickets MODIFY COLUMN status VARCHAR(32) NOT NULL",
                "ALTER TABLE tickets MODIFY COLUMN assignment_status VARCHAR(32) NOT NULL",
                "ALTER TABLE sla_policies MODIFY COLUMN priority VARCHAR(32) NOT NULL",
                "ALTER TABLE module_permissions MODIFY COLUMN role VARCHAR(32) NOT NULL",
                "ALTER TABLE module_permissions MODIFY COLUMN module VARCHAR(64) NOT NULL"
        );

        for (String statement : statements) {
            try {
                jdbcTemplate.execute(statement);
            } catch (Exception ignored) {
                // Ignore compatibility fixes when a column/table is not present yet.
            }
        }
    }
}
