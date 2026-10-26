package io.hexlet.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import io.hexlet.spring.dto.ProductDTO;
import io.hexlet.spring.model.Product;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class ProductMapper {

    public abstract ProductDTO map(Product model);
}
