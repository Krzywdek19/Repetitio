package pl.exceptionhandled.repetitio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class RepetitioApplicationTests {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldEnablePgvectorExtension() {
		Integer count = jdbcTemplate.queryForObject(
				"""
					SELECT COUNT(*) 
					FROM pg_extension 
					WHERE extname = 'vector'
					""",
				Integer.class
		);

		assertEquals(1, count);
	}

	@Test
	void contextLoads() {
	}

}
