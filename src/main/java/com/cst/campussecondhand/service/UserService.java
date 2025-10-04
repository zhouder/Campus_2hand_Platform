package com.cst.campussecondhand.service;

import com.cst.campussecondhand.entity.User;
import com.cst.campussecondhand.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.UUID;

import static com.cst.campussecondhand.service.ProductService.UPLOAD_DIR;

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
        String[] colors = {"#f44336", "#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", "#03a9f4", "#00bcd4", "#009688", "#4caf50", "#8bc34a", "#cddc39", "#ffeb3b", "#ffc107", "#ff9800", "#ff5722"};
        String bgColor = colors[(int) Math.floor(Math.random() * colors.length)];
        user.setAvatarBgColor(bgColor);
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

    // 更新用户基本信息
    public User updateUser(Integer userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查邮箱是否已被其他用户注册
        User userByEmail = userRepository.findByEmail(userDetails.getEmail());
        if (userByEmail != null && userByEmail.getId() != userId) {
            throw new RuntimeException("该邮箱已被注册");
        }

        user.setNickname(userDetails.getNickname());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setUpdatedTime(new Date());

        return userRepository.save(user);
    }

    // 修改密码
    public void updatePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("旧密码错误");
        }

        user.setPassword(newPassword);
        user.setUpdatedTime(new Date());
        userRepository.save(user);
    }

    // 新增：更新用户头像
    public User updateAvatar(Integer userId, MultipartFile avatarFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 处理文件上传
        String uniqueFileName = UUID.randomUUID().toString() + "_" + avatarFile.getOriginalFilename();
        try {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            Files.copy(avatarFile.getInputStream(), Paths.get(UPLOAD_DIR + uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("头像上传失败", e);
        }

        String avatarUrl = "/uploads/" + uniqueFileName;
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedTime(new Date());

        return userRepository.save(user);
    }


    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

}
