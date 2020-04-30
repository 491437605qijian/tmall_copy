package com.how2j.tmall.service;

import com.how2j.tmall.dao.OrderItemDAO;
import com.how2j.tmall.pojo.Order;
import com.how2j.tmall.pojo.OrderItem;
import com.how2j.tmall.pojo.Product;
import com.how2j.tmall.pojo.User;
import com.how2j.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="orderItems")
public class OrderItemService {
    @Autowired
    OrderItemDAO orderItemDAO;
    @Autowired
    ProductImageService productImageService;

    public void fill(List<Order> orders) {
        for (Order order : orders)
            fill(order);
    }

    //订单项填充，还有订单的总金额和总数量
    public void fill(Order order) {
        //如果在xxService的一个方法里，调用另一个 缓存管理 的方法，不能够直接调用，需要通过一个工具，再拿一次xxService然后再调用。
        //例如：注解是之前写法，现在更换缓存写法
        //List<OrderItem> orderItems = listByOrder(order);
        OrderItemService orderItemService = SpringContextUtil.getBean(OrderItemService.class);
        List<OrderItem> orderItems = orderItemService.listByOrder(order);

        float total = 0;
        int totalNumber = 0;
        for (OrderItem oi :orderItems) {
            total+=oi.getNumber()*oi.getProduct().getPromotePrice();
            totalNumber+=oi.getNumber();
            productImageService.setFirstProdutImage(oi.getProduct());
        }
        order.setTotal(total);
        order.setOrderItems(orderItems);
        order.setTotalNumber(totalNumber);
    }

    @Cacheable(key="'orderItems-oid-'+ #p0.id")
    public List<OrderItem> listByOrder(Order order) {
        return orderItemDAO.findByOrderOrderByIdDesc(order);
    }

    public int getSaleCount(Product product) {
        List<OrderItem> ois =listByProduct(product);
        int result =0;
        //订单里面的产品卖出去了，则销量加1
        for (OrderItem oi : ois) {
            if(null!= oi.getOrder() && null!=oi.getOrder().getPayDate())
                result+=oi.getNumber();
        }
        return result;
    }

    @Cacheable(key="'orderItems-pid-'+ #p0.id")
    public List<OrderItem> listByProduct(Product product) {
        return orderItemDAO.findByProduct(product);
    }

    @Cacheable(key="'orderItems-uid-'+ #p0.id")
    public List<OrderItem> listByUser(User user) {
        return orderItemDAO.findByUserAndOrderIsNull(user);
    }

    @CacheEvict(allEntries=true)
    public void update(OrderItem orderItem) {
        orderItemDAO.save(orderItem);
    }

    @CacheEvict(allEntries=true)
    public void add(OrderItem orderItem) {
        orderItemDAO.save(orderItem);
    }

    @Cacheable(key="'orderItems-one-'+ #p0")
    public OrderItem get(int id) {
        return orderItemDAO.findOne(id);
    }

    @CacheEvict(allEntries=true)
    public void delete(int id) {
        orderItemDAO.delete(id);
    }
}
