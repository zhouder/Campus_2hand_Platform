package com.cst.campussecondhand.service;

import com.cst.campussecondhand.entity.Product;
import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.repository.ProductRepository;
import com.cst.campussecondhand.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public static final String UPLOAD_DIR = "src/main/resources/static/uploads/"; // 上传的图片存储地址


    public Product createProduct(Product product, Integer sellerId, MultipartFile[] files) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("卖家用户不存在, ID: " + sellerId));
        // 处理图片上传
        List<String> imageUrls = new ArrayList<>();
        if (files != null && files.length > 0) {
            File uploadDir = new File(UPLOAD_DIR);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                // 生成唯一文件名
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                try {
                    Files.copy(file.getInputStream(), Paths.get(UPLOAD_DIR + uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
                    imageUrls.add("/uploads/" + uniqueFileName);
                } catch (IOException e) {
                    throw new RuntimeException("文件上传失败: " + file.getOriginalFilename(), e);
                }
            }
        }

        product.setSeller(seller);
        product.setCreatedTime(new Date());
        // 将图片URL列表合并成一个字符串
        product.setImageUrls(String.join(",", imageUrls));

        return productRepository.save(product);
    }
}

