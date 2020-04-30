package com.how2j.tmall.web;

import com.how2j.tmall.pojo.Order;
import com.how2j.tmall.service.OrderItemService;
import com.how2j.tmall.service.OrderService;
import com.how2j.tmall.util.Page4Navigator;
import com.how2j.tmall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;

@RestController
public class OrderController {
    @Autowired OrderService orderService;
    @Autowired OrderItemService orderItemService;

    @GetMapping("/orders")
    public Page4Navigator<Order> list(@RequestParam(value = "start", defaultValue = "0") int start,@RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        //分页查询（包含查询订单列表，查询订单项）
        start = start<0?0:start;
        Page4Navigator<Order> page =orderService.list(start, size, 5);
        orderItemService.fill(page.getContent());
        //避免产生无穷递归
        orderService.removeOrderFromOrderItem(page.getContent());

        return page;
    }

    //发货功能（修改状态和日期）
    @PutMapping("deliveryOrder/{oid}")
    public Object deliveryOrder(@PathVariable int oid) throws IOException {
        Order o = orderService.get(oid);
        o.setDeliveryDate(new Date());
        o.setStatus(OrderService.waitConfirm);
        orderService.update(o);
        return Result.success();
    }
}
