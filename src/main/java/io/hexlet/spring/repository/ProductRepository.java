package io.hexlet.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.hexlet.spring.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
