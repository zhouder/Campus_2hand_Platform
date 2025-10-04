package com.cst.campussecondhand.service;

import com.cst.campussecondhand.entity.Favorite;
import com.cst.campussecondhand.entity.Product;
import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.repository.FavoriteRepository;
import com.cst.campussecondhand.repository.ProductRepository;
import com.cst.campussecondhand.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public boolean toggleFavorite(Integer userId, Integer productId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("商品不存在"));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndProduct(user, product);

        if (existingFavorite.isPresent()) {
            // 如果已收藏，则取消收藏
            favoriteRepository.delete(existingFavorite.get());
            product.setFavoriteCount(product.getFavoriteCount() - 1);
            productRepository.save(product);
            return false; // 返回 false 表示取消收藏
        } else {
            // 如果未收藏，则添加收藏
            favoriteRepository.save(new Favorite(user, product));
            product.setFavoriteCount(product.getFavoriteCount() + 1);
            productRepository.save(product);
            return true; // 返回 true 表示添加收藏
        }
    }
}