package com.cst.campussecondhand.controller;

import com.cst.campussecondhand.entity.Product;
import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            HttpSession session) {

        // 检查 session 中是否存在登录用户
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "请先登录再发布商品"));
        }

        // （安全增强）确保发布者ID与当前登录用户ID一致
        if (loggedInUser.getId() != sellerId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "无法替他人发布商品"));
        }

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
     * 处理获取所有商品的GET请求，支持搜索、筛选和排序
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProducts(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "category", required = false, defaultValue = "all") String category,
            @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy) {

        List<Product> products = productService.findProducts(keyword, category, sortBy);

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


    /**
     * 处理获取单个商品的GET请求
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        try {
            Product product = productService.findProductById(id);
            // 为了安全，不直接返回整个 User 对象，而是构造一个 Map
            Map<String, Object> productMap = new java.util.HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("price", product.getPrice());
            productMap.put("description", product.getDescription());
            productMap.put("imageUrls", product.getImageUrls() != null ? product.getImageUrls().split(",") : new String[0]);
            productMap.put("location", product.getLocation());
            productMap.put("category", product.getCategory());
            productMap.put("createdTime", product.getCreatedTime());

            // 构造卖家信息
            Map<String, Object> sellerInfo = new java.util.HashMap<>();
            sellerInfo.put("id", product.getSeller().getId());
            sellerInfo.put("nickname", product.getSeller().getNickname());
            sellerInfo.put("avatarUrl", product.getSeller().getAvatarUrl());
            productMap.put("seller", sellerInfo);

            return ResponseEntity.ok(productMap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

}