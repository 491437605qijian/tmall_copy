package com.how2j.tmall.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.how2j.tmall.pojo.Category;
import com.how2j.tmall.pojo.Property;

public interface PropertyDAO extends JpaRepository<Property,Integer>{
    //根据分类查询，且带分页
    Page<Property> findByCategory(Category category, Pageable pageable);

    //用于PropertyValue里面的初始化产品属性值
    List<Property> findByCategory(Category category);
}