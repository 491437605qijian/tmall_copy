package com.how2j.tmall.service;

import com.how2j.tmall.dao.OrderDAO;
import com.how2j.tmall.pojo.Order;
import com.how2j.tmall.pojo.OrderItem;
import com.how2j.tmall.pojo.User;
import com.how2j.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@CacheConfig(cacheNames="orders")
public class OrderService {
    //状态值修改
    public static final String waitPay = "waitPay";
    public static final String waitDelivery = "waitDelivery";
    public static final String waitConfirm = "waitConfirm";
    public static final String waitReview = "waitReview";
    public static final String finish = "finish";
    public static final String delete = "delete";

    @Autowired OrderDAO orderDAO;
    @Autowired OrderItemService orderItemService;

    @Cacheable(key="'orders-page-'+#p0+ '-' + #p1")
    public Page4Navigator<Order> list(int start, int size, int navigatePages) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size,sort);
        Page pageFromJPA =orderDAO.findAll(pageable);
        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }

    public void removeOrderFromOrderItem(List<Order> orders) {
        for (Order order : orders) {
            removeOrderFromOrderItem(order);
        }
    }

    public void removeOrderFromOrderItem(Order order) {
        List<OrderItem> orderItems= order.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(null);
        }
    }

    @Cacheable(key="'orders-one-'+ #p0")
    public Order get(int oid) {
        return orderDAO.findOne(oid);
    }

    @CacheEvict(allEntries=true)
    public void update(Order bean) {
        orderDAO.save(bean);
    }

    //事务（因为涉及到两个方面的添加，要保持数据的一致性）
    @CacheEvict(allEntries=true)
    @Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
    public float add(Order order, List<OrderItem> ois) {
        //总价格（事务结束会需要带到前台）
        float total = 0;
        //添加订单
        add(order);
        //捕获异常
        if(false)
            throw new RuntimeException();

        //修改该订单里的订单项的orderid
        for (OrderItem oi: ois) {
            oi.setOrder(order);
            orderItemService.update(oi);
            total+=oi.getProduct().getPromotePrice()*oi.getNumber();
        }
        return total;
    }

    @CacheEvict(allEntries=true)
    public void add(Order order) {
        orderDAO.save(order);
    }

    public List<Order> listByUserWithoutDelete(User user) {
        //订单，订单项填充
        List<Order> orders = listByUserAndNotDeleted(user);
        orderItemService.fill(orders);
        return orders;
    }

    @Cacheable(key="'orders-uid-'+ #p0.id")
    public List<Order> listByUserAndNotDeleted(User user) {
        return orderDAO.findByUserAndStatusNotOrderByIdDesc(user, OrderService.delete);
    }

    //计算订单总金额
    public void cacl(Order o) {
        List<OrderItem> orderItems = o.getOrderItems();
        float total = 0;
        for (OrderItem orderItem : orderItems) {
            total+=orderItem.getProduct().getPromotePrice()*orderItem.getNumber();
        }
        o.setTotal(total);
    }
}
