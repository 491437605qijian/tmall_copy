package com.how2j.tmall.web;

import com.how2j.tmall.comparator.*;
import com.how2j.tmall.pojo.*;
import com.how2j.tmall.service.*;
import com.how2j.tmall.util.Result;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

//前台controller（所有方法都写在里面）
@RestController
public class ForeRESTController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    @GetMapping("/forehome")
    public Object home() {
        //分类列表
        List<Category> cs= categoryService.list();
        //分类产品列表
        productService.fill(cs);
        //分类推荐产品列表
        productService.fillByRow(cs);
        //防止递归
        categoryService.removeCategoryFromProduct(cs);
        return cs;
    }

    //注册方法
    @PostMapping("/foreregister")
    public Object register(@RequestBody User user) {
        String name =  user.getName();
        String password = user.getPassword();
        //名称特殊符号转义
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        //判断重复
        boolean exist = userService.isExist(name);
        if(exist){
            String message ="用户名已经被使用,不能使用";
            return Result.fail(message);
        }
        //盐加密（Shiro）
        String salt = new SecureRandomNumberGenerator().nextBytes().toString();
        int times = 2;
        String algorithmName = "md5";
        //加密后的密码
        String encodedPassword = new SimpleHash(algorithmName, password, salt, times).toString();
        //存储密码和盐
        user.setSalt(salt);
        user.setPassword(encodedPassword);
        userService.add(user);
        return Result.success();
    }

    //登录方法
    @PostMapping("/forelogin")
    public Object login(@RequestBody User userParam, HttpSession session) {
        //名称特殊符号转义
        String name =  userParam.getName();
        name = HtmlUtils.htmlEscape(name);
        //Shiro的登录验证
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, userParam.getPassword());
        try {
            subject.login(token);
            User user = userService.getByName(name);
//          subject.getSession().setAttribute("user", user);
            session.setAttribute("user", user);
            return Result.success();
        } catch (AuthenticationException e) {
            String message ="账号密码错误";
            return Result.fail(message);
        }
    }

    //产品页面
    @GetMapping("/foreproduct/{pid}")
    public Object product(@PathVariable("pid") int pid) {
        Product product = productService.get(pid);

        //存储图片（单个列表，详情列表）
        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);
        //存储第一张图展示
        productImageService.setFirstProdutImage(product);
        //存储评价数量
        productService.setSaleAndReviewNumber(product);

        //存储产品属性值
        List<PropertyValue> pvs = propertyValueService.list(product);
        //存储评价信息
        List<Review> reviews = reviewService.list(product);

        Map<String,Object> map= new HashMap<>();
        map.put("product", product);
        map.put("pvs", pvs);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    //分类页面展示
    @GetMapping("forecategory/{cid}")
    public Object category(@PathVariable int cid,String sort) {
        //获取分类
        Category c = categoryService.get(cid);
        //填充分类下的产品列表
        productService.fill(c);
        //存储评价数量
        productService.setSaleAndReviewNumber(c.getProducts());
        //防止递归
        categoryService.removeCategoryFromProduct(c);

        //排序
        if(null!=sort){
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }

        return c;
    }

    //搜索方法
    @PostMapping("foresearch")
    public Object search( String keyword){
        //目前是基于数据库的模糊查询
        if(null==keyword)
            keyword = "";
        List<Product> ps= productService.search(keyword,0,20);
        //第一张图
        productImageService.setFirstProdutImages(ps);
        //销量和评价数
        productService.setSaleAndReviewNumber(ps);
        return ps;
    }

    //生成订单项，进入结算页面方法
    @GetMapping("forebuyone")
    public Object buyone(int pid, int num, HttpSession session) {
        return buyoneAndAddCart(pid,num,session);
    }

    private int buyoneAndAddCart(int pid, int num, HttpSession session) {
        Product product = productService.get(pid);
        User user =(User)  session.getAttribute("user");
        //声明接下来跳转页面用到的订单项id
        int oiid = 0;
        //判断是否已经添加订单项
        boolean found = false;
        //如果已经有该订单项，则修改数量
        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId()==product.getId()){
                oi.setNumber(oi.getNumber()+num);
                orderItemService.update(oi);
                found = true;
                oiid = oi.getId();
                break;
            }
        }
        //如果没有，就新增
        if(!found){
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setProduct(product);
            oi.setNumber(num);
            orderItemService.add(oi);
            oiid = oi.getId();
        }
        return oiid;
    }

    //加入购物车方法，其实就是生成订单项
    @GetMapping("foreaddCart")
    public Object addCart(int pid, int num, HttpSession session) {
        buyoneAndAddCart(pid,num,session);
        return Result.success();
    }

    //购物车页面数据加载
    @GetMapping("forecart")
    public Object cart(HttpSession session) {
        User user =(User)  session.getAttribute("user");
        List<OrderItem> ois = orderItemService.listByUser(user);
        productImageService.setFirstProdutImagesOnOrderItems(ois);
        return ois;
    }

    //购物车页面某订单项数量修改
    @GetMapping("forechangeOrderItem")
    public Object changeOrderItem( HttpSession session, int pid, int num) {
        //未登录跳转到login页面
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");

        //订单项》匹对》修改数量
        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId()==pid){
                oi.setNumber(num);
                orderItemService.update(oi);
                break;
            }
        }
        return Result.success();
    }

    //购物车页面某订单项删除
    @GetMapping("foredeleteOrderItem")
    public Object deleteOrderItem(HttpSession session,int oiid){
        //未登录跳转到login页面
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        //删除订单项
        orderItemService.delete(oiid);
        return Result.success();
    }

    //结算页面数据展示
    @GetMapping("forebuy")
    public Object buy(String[] oiid,HttpSession session){
        List<OrderItem> orderItems = new ArrayList<>();
        //总计
        float total = 0;

        for (String strid : oiid) {
            int id = Integer.parseInt(strid);
            OrderItem oi= orderItemService.get(id);
            total +=oi.getProduct().getPromotePrice()*oi.getNumber();
            orderItems.add(oi);
        }
        //设置图片
        productImageService.setFirstProdutImagesOnOrderItems(orderItems);

        Map<String,Object> map = new HashMap<>();
        map.put("orderItems", orderItems);
        map.put("total", total);
        //将数据放入session
        session.setAttribute("ois", orderItems);
        return Result.success(map);
    }

    //提交订单按钮
    @PostMapping("forecreateOrder")
    public Object createOrder(@RequestBody Order order,HttpSession session){
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        //根据当前时间加上一个4位随机数生成订单号
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUser(user);
        //待支付状态
        order.setStatus(OrderService.waitPay);
        //获取之前的session存储的订单项
        List<OrderItem> ois= (List<OrderItem>)  session.getAttribute("ois");
        //跟数据库打交道
        float total =orderService.add(order,ois);

        Map<String,Object> map = new HashMap<>();
        map.put("oid", order.getId());
        map.put("total", total);

        return Result.success(map);
    }

    //支付成功页面数据更新，并带到前台order进行数据加载
    @GetMapping("forepayed")
    public Object payed(int oid) {
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        return order;
    }

    //我的订单页面数据获取
    @GetMapping("forebought")
    public Object bought(HttpSession session) {
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        //获取数据并防止递归
        List<Order> os= orderService.listByUserWithoutDelete(user);
        orderService.removeOrderFromOrderItem(os);
        return os;
    }

    //我的订单页面删除订单（这里只是修改状态）
    @PutMapping("foredeleteOrder")
    public Object deleteOrder(int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return Result.success();
    }

    //确认收货页面数据加载
    @GetMapping("foreconfirmPay")
    public Object confirmPay(int oid) {
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        orderService.cacl(o);
        orderService.removeOrderFromOrderItem(o);
        return o;
    }

    //确认收货成功，状态修改
    @GetMapping("foreorderConfirmed")
    public Object orderConfirmed( int oid) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return Result.success();
    }

    //评价页面数据加载
    @GetMapping("forereview")
    public Object review(int oid) {
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        orderService.removeOrderFromOrderItem(o);

        Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewService.list(p);

        productService.setSaleAndReviewNumber(p);
        Map<String,Object> map = new HashMap<>();
        map.put("p", p);
        map.put("o", o);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    //评价
    @PostMapping("foredoreview")
    public Object doreview( HttpSession session,int oid,int pid,String content) {
        //先修改其账单状态
        Order o = orderService.get(oid);
        o.setStatus(OrderService.finish);
        orderService.update(o);

        Product p = productService.get(pid);
        content = HtmlUtils.htmlEscape(content);
        //跟数据库打交道
        User user =(User)  session.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setProduct(p);
        review.setCreateDate(new Date());
        review.setUser(user);
        reviewService.add(review);
        return Result.success();
    }

    //检验是否已登录状态，登录返回success
    //两处用到：加入购物车、购买
    @GetMapping("forecheckLogin")
    public Object checkLogin( HttpSession session) {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated())
            return Result.success();
        else
            return Result.fail("未登录");
    }

}
