package com.cst.campussecondhand.controller;

import com.cst.campussecondhand.entity.Product;
import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.repository.FavoriteRepository; // 确保导入
import com.cst.campussecondhand.service.ProductService;
import jakarta.servlet.http.HttpSession; // 确保导入
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

    // 注入我们之前创建的 FavoriteRepository
    @Autowired
    private FavoriteRepository favoriteRepository;

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
            HttpSession session) { // 这个方法之前已经加好了

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "请先登录再发布商品"));
        }
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
            @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy,
            HttpSession session) { // <--- 错误修正：在这里添加 HttpSession session 参数

        List<Product> products = productService.findProducts(keyword, category, sortBy);
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        List<Map<String, Object>> productsResponse = products.stream().map(product -> {
            Map<String, Object> productMap = new java.util.HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("price", product.getPrice());
            productMap.put("favoriteCount", product.getFavoriteCount());

            boolean isFavorited = false;
            if (loggedInUser != null) {
                isFavorited = favoriteRepository.existsByUserAndProduct(loggedInUser, product);
            }
            productMap.put("isFavorited", isFavorited);

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
    public ResponseEntity<?> getProductById(@PathVariable Integer id, HttpSession session) { // <--- 错误修正：在这里也添加 HttpSession session 参数
        try {
            Product product = productService.findProductById(id);
            User loggedInUser = (User) session.getAttribute("loggedInUser");

            Map<String, Object> productMap = new java.util.HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("price", product.getPrice());
            productMap.put("description", product.getDescription());
            productMap.put("imageUrls", product.getImageUrls() != null ? product.getImageUrls().split(",") : new String[0]);
            productMap.put("location", product.getLocation());
            productMap.put("category", product.getCategory());
            productMap.put("createdTime", product.getCreatedTime());
            productMap.put("favoriteCount", product.getFavoriteCount());

            boolean isFavorited = false;
            if (loggedInUser != null) {
                isFavorited = favoriteRepository.existsByUserAndProduct(loggedInUser, product);
            }
            productMap.put("isFavorited", isFavorited);

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

    // 新增：获取当前登录用户发布的所有商品
    @GetMapping("/my")
    public ResponseEntity<?> getMyProducts(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "用户未登录"));
        }
        List<Product> myProducts = productService.findProductsBySellerId(loggedInUser.getId());

        // 将商品列表转换为包含封面图的Map列表，方便前端处理
        List<Map<String, Object>> productsResponse = myProducts.stream().map(product -> {
            Map<String, Object> productMap = new java.util.HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("price", product.getPrice());
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                productMap.put("coverImage", product.getImageUrls().split(",")[0]);
            } else {
                productMap.put("coverImage", null);
            }
            return productMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(productsResponse);
    }

    // 修改：更新一个商品的接口
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer id,
            @RequestParam("title") String title,
            @RequestParam("price") String priceString, // 1. 将价格接收为字符串
            @RequestParam(value = "description", required = false) String description, // 2. 将描述设为可选
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam(value = "existingImageUrls", required = false) List<String> existingImageUrls,
            @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
            HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "请先登录"));
        }

        try {
            // 3. 在 try-catch 内部手动转换价格，增强代码健壮性
            BigDecimal price;
            if (priceString == null || priceString.isBlank()) {
                throw new RuntimeException("价格不能为空");
            }
            try {
                price = new BigDecimal(priceString);
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("价格不能为负数");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("价格格式无效，请输入有效的数字");
            }

            Product product = productService.findProductById(id);
//            if (!product.getSeller().getId().equals(loggedInUser.getId())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "无权修改他人商品"));
//            }

            Product productDetails = new Product();
            productDetails.setTitle(title);
            productDetails.setPrice(price); // 使用我们转换好的价格
            productDetails.setDescription(description);
            productDetails.setCategory(category);
            productDetails.setLocation(location);

            Product updatedProduct = productService.updateProduct(id, productDetails, existingImageUrls, newImages);
            return ResponseEntity.ok(updatedProduct);

        } catch (RuntimeException e) {
            // 现在，任何转换错误或逻辑错误都会被这里捕获，并返回400错误
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 新增：删除一个商品（下架）
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "请先登录"));
        }
        try {
            Product product = productService.findProductById(id);
            if (product.getSeller().getId() != loggedInUser.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", "无权删除他人商品"));
            }
            productService.deleteProduct(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "商品下架成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}