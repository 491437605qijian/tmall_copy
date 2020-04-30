package com.how2j.tmall.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.how2j.tmall.pojo.User;

public interface UserDAO extends JpaRepository<User,Integer>{
    //根据名称查询user（检验用户名重复）
    User findByName(String name);
    //根据名称和密码查询user（验证登录）
    User getByNameAndPassword(String name, String password);
}
