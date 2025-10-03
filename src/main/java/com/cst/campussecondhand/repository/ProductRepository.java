package com.cst.campussecondhand.repository;

import com.cst.campussecondhand.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> { }
