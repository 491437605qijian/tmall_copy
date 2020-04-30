package com.how2j.tmall.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.how2j.tmall.pojo.Product;
import com.how2j.tmall.util.ImageUtil;
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

import com.how2j.tmall.dao.CategoryDAO;
import com.how2j.tmall.pojo.Category;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;


@Service
//Redis的缓存，我们都会在 Service 这一层上面做
//这里注意，第一次进方法取数据的时候，不是从缓存中取出来，因为要放进缓存，而第二次取数据就可以从缓存取啦

//表示分类在缓存里的keys，都是归 "categories" 这个管理的
//通过工具Redis 图形界面客户端 可以看到有一个 categories~keys, 就是它，用于维护分类信息在 redis里都有哪些 key
@CacheConfig(cacheNames="categories")
public class CategoryService {

    @Autowired
    CategoryDAO categoryDAO;

    //默认查询
    @Cacheable(key="'categories-all'")
    public List<Category> list() {
        //查询所有，根据id倒序
        Sort sort = new Sort(Sort.Direction.ASC, "id");
        return categoryDAO.findAll(sort);
    }
    //#p0 #p1标识第一个入参和第二个入参，接下来的方法一致
    @Cacheable(key="'categories-page-'+#p0+ '-' + #p1")
    public Page4Navigator<Category> list(int start, int size, int navigatePages) {
        //start当前第几页，size一页多少记录，navigatePages下方展示方式（例如：navigatePages = 7，下方展示7个）
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size,sort);
        Page pageFromJPA =categoryDAO.findAll(pageable);

        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }


    //新增，其中图片不存在数据库，是通过[id].jpg存储在项目中进行读取
    //增、删、改的注解都是这个，原因是增删改涉及到数据的改变，要重新更新缓存，此方法是清除缓存数据
    @CacheEvict(allEntries=true)
    public void add(Category bean) {
        categoryDAO.save(bean);
    }

    //上面新增方法的图片存取
    public void saveOrUpdateImageFile(Category bean, MultipartFile image, HttpServletRequest request)
            throws IOException {
        File imageFolder= new File(request.getServletContext().getRealPath("img/category"));
        File file = new File(imageFolder,bean.getId()+".jpg");
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        image.transferTo(file);
        BufferedImage img = ImageUtil.change2jpg(file);
        ImageIO.write(img, "jpg", file);
    }

    //删除方法
    @CacheEvict(allEntries=true)
    public void delete(int id) {
        categoryDAO.delete(id);
    }

    //删除相应的图片
    public void deleteImg(int id, HttpServletRequest request) {
        File imageFolder= new File(request.getServletContext().getRealPath("img/category"));
        File file = new File(imageFolder,id+".jpg");
        file.delete();
    }

    //获取一条数据的缓存对应key
    @Cacheable(key="'categories-one-'+ #p0")
    //根据id获取单条数据
    public Category get(int id) {
        Category c= categoryDAO.findOne(id);
        return c;
    }

    //进行修改
    @CacheEvict(allEntries=true)
    public void update(Category bean) {
        categoryDAO.save(bean);
    }

    public void removeCategoryFromProduct(List<Category> cs) {
        for (Category category : cs) {
            removeCategoryFromProduct(category);
        }
    }

    //避免产生无穷递归
    public void removeCategoryFromProduct(Category category) {
        List<Product> products =category.getProducts();
        if(null!=products) {
            for (Product product : products) {
                product.setCategory(null);
            }
        }

        List<List<Product>> productsByRow =category.getProductsByRow();
        if(null!=productsByRow) {
            for (List<Product> ps : productsByRow) {
                for (Product p: ps) {
                    p.setCategory(null);
                }
            }
        }
    }
}
