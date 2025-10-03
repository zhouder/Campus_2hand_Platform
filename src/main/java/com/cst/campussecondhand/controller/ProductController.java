package com.cst.campussecondhand.controller;

import com.cst.campussecondhand.entity.Product;
import com.cst.campussecondhand.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestParam("title") String title,
            @RequestParam("price") BigDecimal price,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam("sellerId") Integer sellerId,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        try {
            Product product = new Product();
            product.setTitle(title);
            product.setPrice(price);
            product.setDescription(description);
            product.setCategory(category);
            product.setLocation(location);

            Product createdProduct = productService.createProduct(product, sellerId, images);
            return ResponseEntity.ok(createdProduct);
        } catch (RuntimeException e) {
            Map<String, String> error = Collections.singletonMap("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

