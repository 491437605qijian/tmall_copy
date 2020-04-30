package com.how2j.tmall.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.how2j.tmall.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

//登录拦截器，如果用户未登录直接进购物车，则要跳转至login页面，其他页面以此类推
//注册，登录，产品，首页，分类，查询等等，可以不用登录查看
//购买行为，加入购物车行为，查看购物车，查看我的订单等等，要进行登录
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        HttpSession session = httpServletRequest.getSession();
        String contextPath=session.getServletContext().getContextPath();
        String[] requireAuthPages = new String[]{
                "buy",
                "alipay",
                "payed",
                "cart",
                "bought",
                "confirmPay",
                "orderConfirmed",

                "forebuyone",
                "forebuy",
                "foreaddCart",
                "forecart",
                "forechangeOrderItem",
                "foredeleteOrderItem",
                "forecreateOrder",
                "forepayed",
                "forebought",
                "foreconfirmPay",
                "foreorderConfirmed",
                "foredeleteOrder",
                "forereview",
                "foredoreview"

        };

        String uri = httpServletRequest.getRequestURI();
        uri = StringUtils.remove(uri, contextPath+"/");
        String page = uri;
        if(begingWith(page, requireAuthPages)){
            //未登录传到login页面
            Subject subject = SecurityUtils.getSubject();
            if(!subject.isAuthenticated()) {
                httpServletResponse.sendRedirect("login");
                return false;
            }
        }
        return true;
    }

    //去掉/tmall前缀之后的uri的前缀是否和那些需要登录才能进行操作的方法地址相同
    //如果相同，返回true
    private boolean begingWith(String page, String[] requiredAuthPages) {
        boolean result = false;
        for (String requiredAuthPage : requiredAuthPages) {
            if(StringUtils.startsWith(page, requiredAuthPage)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}