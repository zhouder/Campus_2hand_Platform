package com.cst.campussecondhand.controller;

import com.cst.campussecondhand.entity.Product;
import com.cst.campussecondhand.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 处理创建新商品的POST请求
     */
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

    /**
     * 处理获取所有商品的GET请求
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        List<Product> products = productService.findAllProducts();
        // 处理数据，避免敏感信息泄露
        List<Map<String, Object>> productsResponse = products.stream().map(product -> {
            Map<String, Object> productMap = new java.util.HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("price", product.getPrice());
            productMap.put("description", product.getDescription());
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                productMap.put("coverImage", product.getImageUrls().split(",")[0]);
            } else {
                productMap.put("coverImage", null);
            }
            productMap.put("location", product.getLocation());
            productMap.put("category", product.getCategory());

            Map<String, Object> sellerInfo = new java.util.HashMap<>();
            sellerInfo.put("id", product.getSeller().getId());
            sellerInfo.put("nickname", product.getSeller().getNickname());
            productMap.put("seller", sellerInfo);

            return productMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(productsResponse);
    }

}

