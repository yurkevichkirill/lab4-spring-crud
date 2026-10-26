package io.hexlet.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hexlet.spring.dto.ProductDTO;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.ProductMapper;
import io.hexlet.spring.model.Product;
import io.hexlet.spring.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product(Long id, String name, String price) {
        var p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        return p;
    }

    private ProductDTO dto(Long id, String name) {
        var d = new ProductDTO();
        d.setId(id);
        d.setName(name);
        return d;
    }

    @Test
    public void testGetAll() {
        var p1 = product(1L, "Product 1", "10.99");
        var p2 = product(2L, "Product 2", "19.99");
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        when(productMapper.map(p1)).thenReturn(dto(1L, "Product 1"));
        when(productMapper.map(p2)).thenReturn(dto(2L, "Product 2"));

        var result = productService.getAll();

        assertThat(result).extracting(ProductDTO::getName).containsExactly("Product 1", "Product 2");
        verify(productRepository).findAll();
    }

    @Test
    public void testFindById() {
        var p = product(1L, "Product 1", "10.99");
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productMapper.map(p)).thenReturn(dto(1L, "Product 1"));

        var result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Product 1");
    }

    @Test
    public void testFindByIdNotFound() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product Not Found: 42");
    }
}
