package com.example.product_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.product_service.model.ProductVariant;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
}
