package com.how2j.tmall.service;

import com.how2j.tmall.dao.ProductImageDAO;
import com.how2j.tmall.pojo.OrderItem;
import com.how2j.tmall.pojo.Product;
import com.how2j.tmall.pojo.ProductImage;
import com.how2j.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="productImages")
public class ProductImageService   {

    //定义常量，区分单一图片和详情图片
    public static final String type_single = "single";
    public static final String type_detail = "detail";

    @Autowired
    ProductImageDAO productImageDAO;
    @Autowired
    ProductService productService;

    @CacheEvict(allEntries=true)
    public void add(ProductImage bean) {
        productImageDAO.save(bean);

    }

    @CacheEvict(allEntries=true)
    public void delete(int id) {
        productImageDAO.delete(id);
    }

    @Cacheable(key="'productImages-one-'+ #p0")
    public ProductImage get(int id) {
        return productImageDAO.findOne(id);
    }

    //单一图片
    @Cacheable(key="'productImages-single-pid-'+ #p0.id")
    public List<ProductImage> listSingleProductImages(Product product) {
        return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_single);
    }

    //详情图片
    @Cacheable(key="'productImages-detail-pid-'+ #p0.id")
    public List<ProductImage> listDetailProductImages(Product product) {
        return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_detail);
    }

    //设置单个图片
    public void setFirstProdutImage(Product product) {
        ProductImageService productImageService = SpringContextUtil.getBean(ProductImageService.class);
        List<ProductImage> singleImages = productImageService.listSingleProductImages(product);
        if(!singleImages.isEmpty())
            //不为空，取第一个ProductImage
            product.setFirstProductImage(singleImages.get(0));
        else
            //这样做是考虑到产品还没有来得及设置图片，但是在订单后台管理里查看订单项的对应产品图片。
            product.setFirstProductImage(new ProductImage());

    }

    //设置多个图片
    public void setFirstProdutImages(List<Product> products) {
        for (Product product : products)
            setFirstProdutImage(product);
    }

    //结算页面每个订单项展示首个图片
    public void setFirstProdutImagesOnOrderItems(List<OrderItem> ois) {
        for (OrderItem orderItem : ois) {
            setFirstProdutImage(orderItem.getProduct());
        }
    }

}
