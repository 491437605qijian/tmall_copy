package com.how2j.tmall.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.how2j.tmall.pojo.Category;

//继承了JpaRepository，可以用里面的很多CRUD和分页的各种常见功能
public interface CategoryDAO extends JpaRepository<Category,Integer>{

}
