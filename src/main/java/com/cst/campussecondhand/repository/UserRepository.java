package com.cst.campussecondhand.repository;

import com.cst.campussecondhand.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    // 自动生成 SQL 查询
    User findByUsername(String username);
    User findByEmail(String email);
}