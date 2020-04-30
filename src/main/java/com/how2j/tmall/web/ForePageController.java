package com.how2j.tmall.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

//前台管理页面跳转专用控制器：用来实现页面跳转，不包含数据传输
@Controller
public class ForePageController {
    //首页
    @GetMapping(value="/")
    public String index(){
        return "redirect:home";
    }
    @GetMapping(value="/home")
    public String home(){
        return "fore/home";
    }
    //注册页面和注册成功页面
    @GetMapping(value="/register")
    public String register(){
        return "fore/register";
    }
    @GetMapping(value="/registerSuccess")
    public String registerSuccess(){
        return "fore/registerSuccess";
    }
    //产品页
    @GetMapping(value="/product")
    public String product(){
        return "fore/product";
    }
    //分类页
    @GetMapping(value="/category")
    public String category(){
        return "fore/category";
    }
    //登录页
    @GetMapping(value="/login")
    public String login(){
        return "fore/login";
    }
    //搜索页
    @GetMapping(value="/search")
    public String searchResult(){
        return "fore/search";
    }
    //结算页面
    @GetMapping(value="/buy")
    public String buy(){
        return "fore/buy";
    }
    //查看购物车
    @GetMapping(value="/cart")
    public String cart(){
        return "fore/cart";
    }
    //确认支付页面
    @GetMapping(value="/alipay")
    public String alipay(){
        return "fore/alipay";
    }
    //支付成功页面
    @GetMapping(value="/payed")
    public String payed(){
        return "fore/payed";
    }
    //我的订单页面
    @GetMapping(value="/bought")
    public String bought(){
        return "fore/bought";
    }
    //确认收货页面
    @GetMapping(value="/confirmPay")
    public String confirmPay(){
        return "fore/confirmPay";
    }
    //确认收货成功页面（可以进行评价）
    @GetMapping(value="/orderConfirmed")
    public String orderConfirmed(){
        return "fore/orderConfirmed";
    }
    //评价页面
    @GetMapping(value="/review")
    public String review(){
        return "fore/review";
    }
    //退出方法，移除user并刷新页面
    @GetMapping("/forelogout")
    public String logout(HttpSession session ) {
        //判断是否有账户，有账户退出需要shiro判断
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated())
            subject.logout();
        return "redirect:home";
    }
}
