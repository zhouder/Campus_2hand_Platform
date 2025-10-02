package com.cst.campussecondhand.service;

import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.Date;

@Service

public class UserService {
    @Autowired
    private UserRepository userRepository;

    // 注册逻辑
    public User register(User user) {
        // 检查该用户名是否已经存在
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名 “"+user.getUsername()+"”已存在");
        }
        // 检查该邮箱是否已经注册
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("该邮箱已注册");
        }
        // 设置创建时间和更新时间
        Date now=new Date();
        user.setCreatedTime(now);
        user.setUpdatedTime(now);
        return userRepository.save(user);
    }

    // 登录逻辑
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 验证密码
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

}
