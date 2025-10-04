package com.cst.campussecondhand.repository;

import com.cst.campussecondhand.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findBySellerIdOrderByCreatedTimeDesc(Integer sellerId);
}
