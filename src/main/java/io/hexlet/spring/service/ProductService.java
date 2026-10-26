package io.hexlet.spring.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hexlet.spring.dto.ProductDTO;
import io.hexlet.spring.exception.ResourceNotFoundException;
import io.hexlet.spring.mapper.ProductMapper;
import io.hexlet.spring.repository.ProductRepository;

/**
 * Чтение продуктов, созданных миграциями Flyway (демонстрация данных из V1.1).
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAll() {
        return productRepository.findAll().stream()
                .map(productMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found: " + id));
        return productMapper.map(product);
    }
}
