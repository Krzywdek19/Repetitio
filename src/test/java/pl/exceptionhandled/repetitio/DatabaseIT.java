package pl.exceptionhandled.repetitio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

class DatabaseIT extends PostgresIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    void migrationsRunOnPostgres() {
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success", Long.class))
                .isGreaterThanOrEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from app_user", Long.class)).isNotNull();
    }
}
