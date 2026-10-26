package io.hexlet.spring.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import io.hexlet.spring.repository.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testIndex() throws Exception {
        var body = mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(3);
        assertThatJson(body).inPath("$[*].name")
                .isArray().containsExactlyInAnyOrder("Product 1", "Product 2", "Product 3");
    }

    @Test
    public void testShow() throws Exception {
        var product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Product 2")).findFirst().orElseThrow();
        var body = mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo("Product 2"),
                v -> v.node("description").isEqualTo("Description of Product 2"),
                v -> v.node("price").isEqualTo(19.99),
                v -> v.node("createdAt").isPresent(),
                v -> v.node("modifiedAt").isPresent());
    }

    @Test
    public void testShowNotFound() throws Exception {
        var body = mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains("Product Not Found: 999999");
    }
}
