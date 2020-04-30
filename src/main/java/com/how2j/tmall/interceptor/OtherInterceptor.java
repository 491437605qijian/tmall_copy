package com.how2j.tmall.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.how2j.tmall.pojo.Category;
import com.how2j.tmall.pojo.OrderItem;
import com.how2j.tmall.pojo.User;
import com.how2j.tmall.service.CategoryService;
import com.how2j.tmall.service.OrderItemService;

public class OtherInterceptor implements HandlerInterceptor {
    @Autowired CategoryService categoryService;
    @Autowired OrderItemService orderItemService;
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //其他的拦截器

        //1、cartTotalItemNumber购物车数量（登录后）
        HttpSession session = httpServletRequest.getSession();
        User user =(User) session.getAttribute("user");
        int  cartTotalItemNumber = 0;
        if(null!=user) {
            List<OrderItem> ois = orderItemService.listByUser(user);
            for (OrderItem oi : ois) {
                cartTotalItemNumber+=oi.getNumber();
            }

        }
        session.setAttribute("cartTotalItemNumber", cartTotalItemNumber);

        //2、categories_below_search搜索框下面的分类展示
        List<Category> cs =categoryService.list();
        httpServletRequest.getServletContext().setAttribute("categories_below_search", cs);

        //3、contextPath回到首页
        String contextPath=httpServletRequest.getServletContext().getContextPath();
        httpServletRequest.getServletContext().setAttribute("contextPath", contextPath);
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
