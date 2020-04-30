package com.how2j.tmall.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.how2j.tmall.pojo.Product;
import com.how2j.tmall.pojo.Property;
import com.how2j.tmall.pojo.PropertyValue;

public interface PropertyValueDAO extends JpaRepository<PropertyValue,Integer>{

    //列表遍历展示
    List<PropertyValue> findByProductOrderByIdDesc(Product product);

    //查询某个产品的某个属性值
    PropertyValue getByPropertyAndProduct(Property property, Product product);

}