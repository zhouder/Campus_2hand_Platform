package com.cst.campussecondhand.controller;

import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Integer productId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "请先登录"));
        }

        try {
            boolean isFavorited = favoriteService.toggleFavorite(loggedInUser.getId(), productId);
            return ResponseEntity.ok(Collections.singletonMap("isFavorited", isFavorited));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}