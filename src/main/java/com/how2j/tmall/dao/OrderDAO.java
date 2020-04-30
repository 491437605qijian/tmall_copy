package com.how2j.tmall.dao;

import com.how2j.tmall.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.how2j.tmall.pojo.Order;

import java.util.List;

public interface OrderDAO extends JpaRepository<Order,Integer>{
    //用来获取那些某个用户的订单，但是状态又不是 "delete" 的订单
    public List<Order> findByUserAndStatusNotOrderByIdDesc(User user, String status);
}
