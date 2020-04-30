package com.how2j.tmall.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.how2j.tmall.pojo.Product;
import com.how2j.tmall.pojo.ProductImage;

public interface ProductImageDAO extends JpaRepository<ProductImage,Integer>{
    //产品图片展示页面，展示了single和detail两种图，所以既要根据pid查询，也要根据type查询
    public List<ProductImage> findByProductAndTypeOrderByIdDesc(Product product, String type);

}
