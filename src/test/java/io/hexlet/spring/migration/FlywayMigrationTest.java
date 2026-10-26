package io.hexlet.spring.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import io.hexlet.spring.model.Product;
import io.hexlet.spring.repository.ProductRepository;

/**
 * Проверяет, что при старте приложения Flyway применил миграции из db/migration.
 */
@SpringBootTest
@ActiveProfiles("test")
public class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testSchemaHistoryContainsProductMigrations() {
        var versions = jdbcTemplate.queryForList(
                "SELECT \"version\" FROM \"flyway_schema_history\" WHERE \"success\" = TRUE ORDER BY \"installed_rank\"",
                String.class);
        assertThat(versions).containsSubsequence("1", "1.1");
        assertThat(versions).contains("2");
    }

    @Test
    public void testNoPendingMigrations() {
        var info = flyway.info();
        assertThat(info.pending()).isEmpty();
        assertThat(info.current().getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    public void testProductTableFilledByMigration() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRODUCT", Integer.class);
        assertThat(count).isEqualTo(3);

        List<String> names = productRepository.findAll().stream().map(Product::getName).toList();
        assertThat(names).containsExactlyInAnyOrder("Product 1", "Product 2", "Product 3");

        var first = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Product 1")).findFirst().orElseThrow();
        assertThat(first.getDescription()).isEqualTo("Description of Product 1");
        assertThat(first.getPrice()).isEqualByComparingTo("10.99");
        assertThat(first.getCreatedAt()).isNotNull();
        assertThat(first.getModifiedAt()).isNotNull();
    }
}
