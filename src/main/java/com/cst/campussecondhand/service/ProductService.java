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

    // 定义图片上传后存储的目录
    public static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    /**
     * 创建一个新商品，并处理图片上传
     * @param product 商品基本信息
     * @param sellerId 卖家ID
     * @param files 上传的图片文件数组
     * @return 创建成功后的商品
     */
    public Product createProduct(Product product, Integer sellerId, MultipartFile[] files) {
        // 1. 查找卖家用户
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("卖家用户不存在, ID: " + sellerId));

        // 2. 处理图片上传
        List<String> imageUrls = new ArrayList<>();
        if (files != null && files.length > 0) {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                // 生成唯一文件名以避免冲突
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                try {
                    // 保存文件到服务器
                    Files.copy(file.getInputStream(), Paths.get(UPLOAD_DIR + uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
                    // 将可访问的URL存入列表
                    imageUrls.add("/uploads/" + uniqueFileName);
                } catch (IOException e) {
                    throw new RuntimeException("文件上传失败: " + file.getOriginalFilename(), e);
                }
            }
        }

        // 3. 将商品信息补充完整
        product.setSeller(seller);
        product.setCreatedTime(new Date());
        // 将图片URL列表合并成一个字符串，用逗号分隔
        product.setImageUrls(String.join(",", imageUrls));

        // 4. 保存到数据库
        return productRepository.save(product);
    }
}

