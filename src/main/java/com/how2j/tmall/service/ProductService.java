package com.how2j.tmall.service;

import com.how2j.tmall.dao.ProductDAO;
import com.how2j.tmall.es.ProductESDAO;
import com.how2j.tmall.pojo.Category;
import com.how2j.tmall.pojo.Product;
import com.how2j.tmall.util.Page4Navigator;
import com.how2j.tmall.util.SpringContextUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@CacheConfig(cacheNames="products")
public class ProductService  {

    @Autowired
    ProductDAO productDAO;
    @Autowired
    ProductESDAO productESDAO;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    @CacheEvict(allEntries=true)
    public void add(Product bean) {
        productDAO.save(bean);
        productESDAO.save(bean);
    }

    @CacheEvict(allEntries=true)
    public void delete(int id) {
        productDAO.delete(id);
        productESDAO.delete(id);
    }

    @Cacheable(key="'products-one-'+ #p0")
    public Product get(int id) {
        return productDAO.findOne(id);
    }

    @CacheEvict(allEntries=true)
    public void update(Product bean) {
        productDAO.save(bean);
        productESDAO.save(bean);
    }

    //如果分页是根据参数分页（如：cid），应该这样写
    @Cacheable(key="'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public Page4Navigator<Product> list(int cid, int start, int size,int navigatePages) {
        //产品关联分类，所以要把分类查出来
        Category category = categoryService.get(cid);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        Page<Product> pageFromJPA =productDAO.findByCategory(category,pageable);
        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }

    //分类里填充产品数据
    public void fill(List<Category> categorys) {
        for (Category category : categorys) {
            fill(category);
        }
    }
    public void fill(Category category) {
        ProductService productService = SpringContextUtil.getBean(ProductService.class);
        List<Product> products = productService.listByCategory(category);
        productImageService.setFirstProdutImages(products);
        category.setProducts(products);
    }

    @Cacheable(key="'products-cid-'+ #p0.id")
    public List<Product> listByCategory(Category category){
        return productDAO.findByCategoryOrderById(category);
    }

    //分类里填充推荐产品数据
    //此方法目的是为了多个产品时切割数据（下面方法按8个切割展示）
    public void fillByRow(List<Category> categorys) {
        //定义一行推荐为8个
        int productNumberEachRow = 8;
        for (Category category : categorys) {
            List<Product> products =  category.getProducts();
            List<List<Product>> productsByRow =  new ArrayList<>();
            for (int i = 0; i < products.size(); i+=productNumberEachRow) {
                //不足8个按原有的展示，超过八个进行分隔
                int size = i+productNumberEachRow;
                size= size>products.size()?products.size():size;
                //subList：按size截取list
                List<Product> productsOfEachRow =products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }

    public void setSaleAndReviewNumber(Product product) {
        int saleCount = orderItemService.getSaleCount(product);
        product.setSaleCount(saleCount);

        int reviewCount = reviewService.getCount(product);
        product.setReviewCount(reviewCount);

    }

    public void setSaleAndReviewNumber(List<Product> products) {
        for (Product product : products)
            setSaleAndReviewNumber(product);
    }

    public List<Product> search(String keyword, int start, int size) {
        /*Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        List<Product> products =productDAO.findByNameLike("%"+keyword+"%",pageable);
        return products;*/

        //初始化elasticsearch数据
        initDatabase2ES();
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
                .add(QueryBuilders.matchPhraseQuery("name", keyword),
                        ScoreFunctionBuilders.weightFactorFunction(100))
                .scoreMode("sum")
                .setMinScore(10);

        Sort sort  = new Sort(Sort.Direction.DESC,"id");
        Pageable pageable = new PageRequest(start, size,sort);
        //执行查询，并取到结果
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(functionScoreQueryBuilder).build();
        Page<Product> page = productESDAO.search(searchQuery);
        return page.getContent();
    }

    //初始化数据到es. 因为数据刚开始都在数据库中，不在es中，所以刚开始查询，先看看es有没有数据
    private void initDatabase2ES() {
        Pageable pageable = new PageRequest(0, 5);
        Page<Product> page =productESDAO.findAll(pageable);
        //如果没有，就把数据从数据库同步到es中
        if(page.getContent().isEmpty()) {
            List<Product> products= productDAO.findAll();
            for (Product product : products) {
                productESDAO.save(product);
            }
        }
    }
}
