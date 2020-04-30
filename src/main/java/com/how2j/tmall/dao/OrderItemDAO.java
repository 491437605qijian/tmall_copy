package com.how2j.tmall.dao;

import java.util.List;

import com.how2j.tmall.pojo.Product;
import com.how2j.tmall.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.how2j.tmall.pojo.Order;
import com.how2j.tmall.pojo.OrderItem;

public interface OrderItemDAO extends JpaRepository<OrderItem,Integer>{
    //根据订单查询该订单的订单项
    List<OrderItem> findByOrderOrderByIdDesc(Order order);
    //根据产品获取OrderItem（用于查询产品销量）
    List<OrderItem> findByProduct(Product product);
    //根据用户查询没有生成订单的订单项（用于判断之前是否已经加入订单项，加入只需更改数量）
    List<OrderItem> findByUserAndOrderIsNull(User user);
}
