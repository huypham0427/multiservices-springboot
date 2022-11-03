package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Creating the Repository to store the data of Product to the database-MongoDB */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
}
