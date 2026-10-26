package io.hexlet.spring.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Продукт. Таблица PRODUCT создаётся и заполняется миграциями Flyway
 * V1__CREATE_PRODUCT_TABLE.sql и V1.1__INSERT_PRODUCT_DATA.sql, а не Hibernate.
 * Даты created_at / modified_at заполняет БД (DEFAULT CURRENT_TIMESTAMP).
 */
@Entity
@Getter
@Setter
@ToString(includeFieldNames = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "PRODUCT")
public class Product implements BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(nullable = false)
    @ToString.Include
    private String name;

    private String description;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime modifiedAt;
}
