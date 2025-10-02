package com.cst.campussecondhand.controller;

import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")

public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try{
            // 注册成功返回一个成功的响应，为了安全，清空密码再返回
            User registeredUser=userService.register(user);
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        }catch (RuntimeException e){
            Map<String,String> error= Collections.singletonMap("error",e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error); // 400
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            User user = userService.login(username, password);
            // 登录成功，返回用户信息（同样，为了安全，清空密码）
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> error = Collections.singletonMap("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);  // 401
        }
    }





}
